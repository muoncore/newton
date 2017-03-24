package io.muoncore.newton.query;

import io.muoncore.newton.DynamicInvokeEventAdaptor;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.OnViewEvent;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.eventsource.TenantEvent;
import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Base class for views that mutate a shared data store.
 * This means that they will use a cluster lock to give process once semantics.
 *
 * This also handles unpacking tenancy information and applying it to the current Thread for
 * use in the mongo filters. As such, it should generally be used within the multi tenant services
 * as the base view.
 */
@Slf4j
public abstract class SharedDatastoreViewService {

	private StreamSubscriptionManager streamSubscriptionManager;
	private DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, OnViewEvent.class);
	private Set<String> subscribedStreams = new HashSet<>();
	private EventStreamProcessor eventStreamProcessor;
	//avoid potential deadlock by doing all work on a different thread, not the event dispatch thread.
	private Executor worker = Executors.newSingleThreadExecutor();

	public SharedDatastoreViewService(StreamSubscriptionManager streamSubscriptionManager, EventStreamProcessor eventStreamProcessor) throws IOException {
		this.streamSubscriptionManager = streamSubscriptionManager;
		this.eventStreamProcessor = eventStreamProcessor;
	}

	private void processStreams() {

		ViewConfiguration[] s = getClass().getAnnotationsByType(ViewConfiguration.class);

		if (s.length == 0) throw new IllegalStateException("View does not have @ViewConfiguration: " + this);

		String[] streams = s[0].streams();

		for(String stream: streams) {
			if (!subscribedStreams.contains(stream)) {
				subscribedStreams.add(stream);
				streamSubscriptionManager.globallyUniqueSubscription(getClass().getSimpleName() + "-" + stream, stream, event -> {
					worker.execute(() -> eventStreamProcessor.executeWithinEventContext(event, this::handleEvent));
				});
			}
		}
	}

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
