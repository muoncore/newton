package io.muoncore.newton.query;

import io.muoncore.newton.DynamicInvokeEventAdaptor;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.OnViewEvent;
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

/**
 * Base class for views that rebuild on startup
 * This means that they will not use a lock of any kind, with each instance running independently
 */
@Slf4j
public abstract class RebuildingDatastoreView extends BaseView {

	private StreamSubscriptionManager streamSubscriptionManager;
	private DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, OnViewEvent.class);
	private Set<String> subscribedStreams = new HashSet<>();
	private EventStreamProcessor eventStreamProcessor;
	//avoid potential deadlock by doing all work on a different thread, not the event dispatch thread.
	private Executor worker = Executors.newSingleThreadExecutor();

  public RebuildingDatastoreView(StreamSubscriptionManager streamSubscriptionManager, EventStreamProcessor eventStreamProcessor) throws IOException {
    super(streamSubscriptionManager, eventStreamProcessor);
  }

  protected Consumer<Consumer<NewtonEvent>> run(String stream) {
    return consumer -> {
      streamSubscriptionManager.localNonTrackingSubscription(stream, consumer);
    };
  }
}
