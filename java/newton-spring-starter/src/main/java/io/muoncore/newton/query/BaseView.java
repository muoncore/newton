package io.muoncore.newton.query;

import io.muoncore.newton.DynamicInvokeEventAdaptor;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.eventsource.AggregateConfiguration;
import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
public abstract class BaseView {

  protected StreamSubscriptionManager streamSubscriptionManager;
  private DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, EventHandler.class);
  private Set<String> subscribedStreams = new HashSet<>();
  private EventStreamProcessor eventStreamProcessor;
  //avoid potential deadlock by doing all work on a different thread, not the event dispatch thread.
  private Executor worker = Executors.newSingleThreadExecutor();

  public BaseView(StreamSubscriptionManager streamSubscriptionManager, EventStreamProcessor eventStreamProcessor) throws IOException {
    this.streamSubscriptionManager = streamSubscriptionManager;
    this.eventStreamProcessor = eventStreamProcessor;
  }

  private void processStreams() {

    NewtonView[] s = getClass().getAnnotationsByType(NewtonView.class);

    if (s.length == 0) throw new IllegalStateException("View does not have @NewtonView: " + this);

    List<String> streams = new ArrayList<>();
    Arrays.stream(s[0].aggregateRoot()).forEach(aClass -> {
      Arrays.stream(aClass.getAnnotationsByType(AggregateConfiguration.class)).findFirst().ifPresent(aggregateConfiguration -> {
        streams.add(aggregateConfiguration.context().concat("/").concat(aClass.getSimpleName()));
      });
    });

    streams.addAll(Arrays.asList(s[0].streams()));

    if (streams.size() == 0){
      throw new IllegalStateException("Invalid configuration. Either 'streams' or 'aggregateRoot' parameter must be specified!");
    }
    for(String stream: streams) {
      if (!subscribedStreams.contains(stream)) {
        subscribedStreams.add(stream);
        run(stream).accept(event -> {
          worker.execute(() -> eventStreamProcessor.executeWithinEventContext(event, this::handleEvent));
        });
      }
    }
  }

  protected abstract Consumer<Consumer<NewtonEvent>> run(String stream);

  private void handleEvent(NewtonEvent event) {
    eventStreamProcessor.executeWithinEventContext(event, newtonEvent -> {
      if (!eventAdaptor.apply(event)) {
        log.debug("View {} did not accept event {}, which discarded by the view", getClass().getName(), event);
      }
    });
  }

  @PostConstruct
  public void initSubscription() throws InterruptedException {
    processStreams();
  }

}
