package io.muoncore.newton.query;

import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.utils.muon.MuonLookupUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class LegacyViewEventStreamSubscriptionTest {

    private StreamSubscriptionManager subscriptionManager = mock(StreamSubscriptionManager.class);
	private ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
	private String applicationName = "test-app";
	private LegacyViewEventStreamSubscription legacyViewEventStreamSubscription = new LegacyViewEventStreamSubscription(subscriptionManager,
            publisher, applicationName);

	@Test
	public void initSubscription() throws Exception {
        //GIVEN
        final Set<String> eventTypes = MuonLookupUtils.listAllAggregateRoots();

        //WHEN
        legacyViewEventStreamSubscription.onApplicationEvent(new ApplicationReadyEvent(new SpringApplication(), null, null));
        //THEN
        ArgumentCaptor<String> eventStreamCaptor = ArgumentCaptor.forClass(String.class);

        verify(subscriptionManager, times(eventTypes.size())).globallyUniqueSubscription(anyString(), eventStreamCaptor.capture(), any(Consumer.class));

        final Iterator<String> eventTypesIter = eventTypes.iterator();
        eventStreamCaptor.getAllValues().forEach(streamName ->
                assertEquals("Stream name", applicationName + "/" + eventTypesIter.next(), streamName)
        );
    }
}
