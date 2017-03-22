package io.muoncore.newton.eventsource.muon;

import io.muoncore.eventstore.TestEventStore;
import io.muoncore.newton.NewtonIdentifier;
import io.muoncore.newton.UUIDIdentifier;
import io.muoncore.newton.eventsource.AggregateNotFoundException;
import io.muoncore.newton.eventsource.OptimisticLockException;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.AggregateEventClient;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.newton.MuonTestConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

//@Category({UnitIntegrationTest.class})
@ActiveProfiles({"test", "log-events"})
@Import({MuonTestConfiguration.class})
@RunWith(SpringRunner.class)
@Configuration
@Ignore
public class MuonEventSourceRepositoryTest {

	@Autowired
	private TestEventStore eventStore;

	@Autowired
	private AggregateEventClient client;

	@Autowired
	private TestEventSourceRepo repository;

	@Test
	public void load() throws Exception {
		NewtonIdentifier id = new UUIDIdentifier();
		client.publishDomainEvents(id.toString(), Collections.singletonList(
			new TestAggregateCreated()
		));

		TestAggregate aggregate = repository.load(id);

		assertEquals(id, aggregate.getId());
	}

	@Test
	public void save() throws Exception {
    UUIDIdentifier id = new UUIDIdentifier();
		TestAggregate customer = new TestAggregate(id);
		repository.save(customer);

		List<Event> events = client.loadAggregateRoot(id.toString());

		assertEquals(1, events.size());
		assertEquals(TestAggregateCreated.class.getSimpleName(), events.get(0).getEventType());
	}

	@Test(expected = AggregateNotFoundException.class)
	public void throwsExceptionOnNonExistingAggregate() {
		repository.load(new UUIDIdentifier());
	}

	@Test(expected = AggregateNotFoundException.class)
	public void withVersionThrowsExceptionOnNonExistingAggregate() {
		repository.load(new UUIDIdentifier(), 5L);
	}

	@Test
	public void canLoadWithVersion() {
    UUIDIdentifier id = new UUIDIdentifier();
		client.publishDomainEvents(id.toString(), Arrays.asList(
			new TestAggregateCreated(),
			new TestAggregateCreated()
		));

		TestAggregate aggregate = repository.load(id, 2L);
		assertEquals(2, aggregate.getVersion());
	}

	@Test(expected = OptimisticLockException.class)
	public void throwsOptimisticLocExceptionOnBadVersion() {
    UUIDIdentifier id = new UUIDIdentifier();
		client.publishDomainEvents(id.toString(), Arrays.asList(
			new TestAggregateCreated()
		));

		repository.load(id, 2L);
	}

	@Component
	public static class TestEventSourceRepo extends MuonEventSourceRepository<TestAggregate> {

		public TestEventSourceRepo(AggregateEventClient aggregateEventClient, EventClient eventClient) {
			super(TestAggregate.class, aggregateEventClient, eventClient, "app-name");
		}
	}

}
