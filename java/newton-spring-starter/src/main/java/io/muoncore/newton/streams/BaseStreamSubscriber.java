package io.muoncore.newton.streams;

import io.muoncore.newton.DynamicInvokeEventAdaptor;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
public abstract class BaseStreamSubscriber {

  protected StreamSubscriptionManager streamSubscriptionManager;
  private DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, EventHandler.class);
  private Set<String> subscribedStreams = new HashSet<>();
  private EventStreamProcessor eventStreamProcessor;
  //avoid potential deadlock by doing all work on a different thread, not the event dispatch thread.
  private Executor worker = Executors.newSingleThreadExecutor();

  public BaseStreamSubscriber(StreamSubscriptionManager streamSubscriptionManager, EventStreamProcessor eventStreamProcessor) throws IOException {
    this.streamSubscriptionManager = streamSubscriptionManager;
    this.eventStreamProcessor = eventStreamProcessor;
  }

  private void processStreams() {

    //todo: re-implement @NewtonView
//    NewtonView[] s = getClass().getAnnotationsByType(NewtonView.class);
//
//    if (s.length == 0) throw new IllegalStateException("View does not have @NewtonView: " + this);
//
//    List<String> eventStreams = new ArrayList<>();
//    Arrays.stream(s[0].aggregateRoot()).forEach(aClass -> {
//      Arrays.stream(aClass.getAnnotationsByType(AggregateConfiguration.class)).findFirst().ifPresent(aggregateConfiguration -> {
//        //todo: parse sPel if required
//        eventStreams.add(aggregateConfiguration.context().concat("/").concat(aClass.getSimpleName()));
//      });
//    });
//
//    eventStreams.addAll(Arrays.asList(s[0].eventStreams()));

    if (eventStreams().length == 0){
      throw new IllegalStateException("Invalid configuration. Either 'eventStreams' or 'aggregateRoot' parameter must be specified!");
    }
    for(String stream: eventStreams()) {
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

  protected abstract String[] eventStreams();

}
