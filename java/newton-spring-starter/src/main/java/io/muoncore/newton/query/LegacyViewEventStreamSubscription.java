package io.muoncore.newton.query;

import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.utils.muon.MuonLookupUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;

/**
 * Serves as a bridge between cqrs event store & legacy spring event bus that have listeners updating views
 */
@Slf4j
public class LegacyViewEventStreamSubscription implements ApplicationListener<ApplicationReadyEvent> {

	private ApplicationEventPublisher publisher;
	private final String applicationName;
	private StreamSubscriptionManager subscriptionManager;

	public LegacyViewEventStreamSubscription(StreamSubscriptionManager streamSubscriptionManager, ApplicationEventPublisher publisher, String applicationName) {
		this.publisher = publisher;
		this.applicationName = applicationName;
		this.subscriptionManager = streamSubscriptionManager;
	}

	@EventListener
    public void onApplicationEvent(ApplicationReadyEvent onReadyEvent) {
		for (String eventType : MuonLookupUtils.listAllAggregateRoots()) {
			String streamName = applicationName.concat("/").concat(eventType);
			log.info("Subscribing to {} for legacy event subscription", streamName);
		    subscriptionManager.globallyUniqueSubscription(
					applicationName.concat("-").concat(eventType),
		            streamName,
                    event -> {
						System.out.println("NewtonEvent received on legacy subscription " + event);
						publisher.publishEvent(event); });
		}
	}
}
