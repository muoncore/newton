package io.muoncore.newton.eventsource.muon;

import io.muoncore.eventstore.TestEventStore;
import io.muoncore.newton.*;
import io.muoncore.newton.eventsource.AggregateNotFoundException;
import io.muoncore.newton.eventsource.OptimisticLockException;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.AggregateEventClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

//@Category({UnitIntegrationTest.class})
@ActiveProfiles({"test", "log-events"})
@Import({MuonTestConfiguration.class})
@RunWith(SpringRunner.class)
@Configuration
@EnableNewton
public class MuonEventSourceRepositoryTest {

	@Autowired
	private TestEventStore eventStore;

	@Autowired
	private AggregateEventClient client;

//	@Autowired
//	private TestEventSourceRepo repository;

  @Autowired
  private MuonEventSourceRepository<TestAggregate> repository;

	@Test
	public void load() throws Exception {
    SimpleAggregateRootId id = new SimpleAggregateRootId();
		client.publishDomainEvents(id.toString(), Collections.singletonList(
			new TestAggregateCreated(id)
		));

		TestAggregate aggregate = repository.load(id);
		assertNotNull(aggregate);

		assertEquals(id.getValue(), aggregate.getId().getValue());
	}

	@Test
	public void save() throws Exception {
    SimpleAggregateRootId id = new SimpleAggregateRootId();
		TestAggregate customer = new TestAggregate(id);
		repository.save(customer);

		List<Event> events = client.loadAggregateRoot(id.toString());

		assertEquals(1, events.size());
		assertEquals(TestAggregateCreated.class.getSimpleName(), events.get(0).getEventType());
	}

	@Test(expected = AggregateNotFoundException.class)
	public void throwsExceptionOnNonExistingAggregate() {
		repository.load(new SimpleAggregateRootId());
	}

	@Test(expected = AggregateNotFoundException.class)
	public void withVersionThrowsExceptionOnNonExistingAggregate() {
		repository.load(new SimpleAggregateRootId(), 5L);
	}

	@Test
	public void canLoadWithVersion() {
    AggregateRootId id = new SimpleAggregateRootId();
		client.publishDomainEvents(id.toString(), Arrays.asList(
			new TestAggregateCreated(),
			new TestAggregateCreated()
		));

		TestAggregate aggregate = repository.load(id, 2L);
		assertEquals(2, aggregate.getVersion());
	}

	@Test(expected = OptimisticLockException.class)
	public void throwsOptimisticLocExceptionOnBadVersion() {
    AggregateRootId id = new SimpleAggregateRootId();
		client.publishDomainEvents(id.toString(), Arrays.asList(
			new TestAggregateCreated()
		));

		repository.load(id, 2L);
	}

  @Test()
  public void canStreamAggregateEvents() throws InterruptedException {
    AggregateRootId id = new SimpleAggregateRootId();
    client.publishDomainEvents(id.toString(), Arrays.asList(
      new TestAggregateCreated(), new TestAggregateCreated()
    ));

    List<NewtonEvent> events = new ArrayList<>();

    repository.replay(id).subscribe(new Subscriber<NewtonEvent>() {
      @Override
      public void onSubscribe(Subscription s) {
        s.request(Integer.MAX_VALUE);
      }

      @Override
      public void onNext(NewtonEvent newtonEvent) {
        System.out.println("HELLO WORLD");
        events.add(newtonEvent);
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onComplete() {

      }
    });

    Thread.sleep(500);

    assertEquals(2, events.size());
  }


//  @Component
//	public static class TestEventSourceRepo extends MuonEventSourceRepository<TestAggregate> {
//
//		public TestEventSourceRepo(AggregateEventClient aggregateEventClient, EventClient eventClient) {
//			super(TestAggregate.class, aggregateEventClient, eventClient, "app-name");
//		}
//	}

}
