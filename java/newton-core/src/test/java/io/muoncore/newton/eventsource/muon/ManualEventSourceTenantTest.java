package io.muoncore.newton.eventsource.muon;

import io.muoncore.newton.AggregateEventClient;
import io.muoncore.newton.eventsource.EventSourceRepository;
import io.muoncore.newton.query.InMemoryQueryConfiguration;
import io.muoncore.protocol.event.client.EventClient;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNotNull;

@ActiveProfiles({"default"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
	ManualEventSourceTenantTest.TestConfiguration.class,
  MuonEventSourceConfiguration.class,
  InMemoryQueryConfiguration.class
})
@Ignore
public class ManualEventSourceTenantTest {

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private EventSourceRepository<TestAggregate> eventSourceRepository;
	@Autowired
	private TestAggregateEventListener testAggregateEventListener;

	@Test
	public void testTenantAggregateRoot() throws InterruptedException {
		IntStream.rangeClosed(0, 100).forEach(i -> {
			eventSourceRepository.save(new TestAggregate("hello-world"));
		});

		Thread.sleep(5000);
        System.out.println("Size is " + testAggregateEventListener.list.size());

        //this will also replay old events, need a teardown.
//		assertEquals("Event stream processed 100 events", 100, testAggregateEventListener.list.size());
		for (TestAggregateCreated event : testAggregateEventListener.list) {
		    System.out.println("Event is " + event);
			final TestAggregate result = eventSourceRepository.load(event.getId());
			assertNotNull("Aggregate stored in event source repo", result);
		}
	}

	@Configuration
	public static class TestConfiguration {
        @Value("${spring.application.name}")
        private String applicationName;

		@Bean
		public TestAggregateEventListener testAggregateEventListener() {
			return new TestAggregateEventListener();
		}

		@Bean
		public EventSourceRepository<TestAggregate> eventSourceRepository(EventClient eventClient, AggregateEventClient aggregateEventClient) {
			return new TestEventSourceRepository(TestAggregate.class, aggregateEventClient, eventClient);
		}
	}

	public static class TestAggregateEventListener {

		private List<TestAggregateCreated> list = new ArrayList<>();

		@EventListener
		public void handle(TestAggregateCreated event) {
			list.add(event);
		}
	}

	public static class TestEventSourceRepository extends MuonEventSourceRepository<TestAggregate> {

		public TestEventSourceRepository(Class<TestAggregate> type, AggregateEventClient aggregateEventClient, EventClient eventClient) {
			super(type, aggregateEventClient, eventClient, new NoOpEventStreamProcessor(), "faked-app");
		}
	}
}

