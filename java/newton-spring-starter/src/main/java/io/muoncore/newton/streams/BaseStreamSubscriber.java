package io.muoncore.newton.streams;

import io.muoncore.newton.DynamicInvokeEventAdaptor;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.StreamSubscriptionManager;
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
  //avoid potential deadlock by doing all work on a different thread, not the event dispatch thread.
  private Executor worker = Executors.newSingleThreadExecutor();

  public BaseStreamSubscriber(StreamSubscriptionManager streamSubscriptionManager/*, EventStreamProcessor eventStreamProcessor*/) throws IOException {
    this.streamSubscriptionManager = streamSubscriptionManager;
  }

  private void processStreams() {
    if (eventStreams() == null || eventStreams().length == 0){
      throw new IllegalStateException("Invalid configuration. EventStreams must be specified!");
    }
    for(String stream: eventStreams()) {
      if (!subscribedStreams.contains(stream)) {
        subscribedStreams.add(stream);
        run(stream).accept(this::handleEvent);
      }
    }
  }

  protected abstract Consumer<Consumer<NewtonEvent>> run(String stream);

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

}
