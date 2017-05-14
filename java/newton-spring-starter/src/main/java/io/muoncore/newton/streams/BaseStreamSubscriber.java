package io.muoncore.newton.streams;

import io.muoncore.newton.*;
import io.muoncore.newton.eventsource.AggregateConfiguration;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseStreamSubscriber {

  protected StreamSubscriptionManager streamSubscriptionManager;
  private DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, EventHandler.class);
  private Set<String> subscribedStreams = new HashSet<>();
  //avoid potential deadlock by doing all work on a different thread, not the event dispatch thread.
  private Executor worker = Executors.newSingleThreadExecutor();

  public BaseStreamSubscriber(StreamSubscriptionManager streamSubscriptionManager/*, EventStreamProcessor eventStreamProcessor*/) throws IOException {
    this.streamSubscriptionManager = streamSubscriptionManager;
  }

  private void processStreams() {
    if (eventStreams() == null || eventStreams().length == 0){
      throw new IllegalStateException("Invalid configuration. EventStreams must be specified!");
    }
    for(String stream: getStreams()) {
      if (!subscribedStreams.contains(stream)) {
        subscribedStreams.add(stream);
        run(stream).accept(this::handleEvent);
      }
    }
  }

  protected String[] getStreams() {
    List<String> streams = new ArrayList<>();

    streams.addAll(Arrays.asList(eventStreams()));
    Arrays.asList(aggregateRoots()).forEach(aggregateRootClass -> {
      Arrays.stream(aggregateRootClass.getAnnotationsByType(AggregateConfiguration.class)).findFirst().ifPresent(aggregateConfiguration -> {
//        //todo: parse sPel if required
        streams.add(aggregateConfiguration.context().concat("/").concat(aggregateRootClass.getSimpleName()));
      });});

    return streams.toArray(new String[streams.size()]);
  }

  private void handleEvent(NewtonEvent event) {
      if (!eventAdaptor.apply(event)) {
        log.debug("View {} did not accept event {}, which discarded by the view", getClass().getName(), event);
      }
  }

  @PostConstruct
  public void initSubscription() throws InterruptedException {
    processStreams();
  }

  protected abstract String[] eventStreams();
  protected abstract Class<AggregateRoot>[] aggregateRoots();
  protected abstract Consumer<Consumer<NewtonEvent>> run(String stream);

}
