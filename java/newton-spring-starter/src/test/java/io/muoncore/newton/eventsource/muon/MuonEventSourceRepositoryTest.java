package io.muoncore.newton.eventsource.muon;

import io.muoncore.eventstore.TestEventStore;
import io.muoncore.newton.MuonTestConfiguration;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.eventsource.AggregateDeletedEvent;
import io.muoncore.newton.eventsource.AggregateNotFoundException;
import io.muoncore.newton.eventsource.GenericAggregateDeletedEvent;
import io.muoncore.newton.eventsource.OptimisticLockException;
import io.muoncore.newton.mongo.MongoConfiguration;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.AggregateEventClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MuonTestConfiguration.class, MongoConfiguration.class})
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
    String id = "simple-id";
		client.publishDomainEvents(id.toString(), Collections.singletonList(
			new TestAggregateCreated(id)
		));

		TestAggregate aggregate = repository.load(id);
		assertNotNull(aggregate);

		assertEquals(id, aggregate.getId());
	}

	@Test
	public void save() throws Exception {
    String id = UUID.randomUUID().toString();
		TestAggregate customer = new TestAggregate(id);
		repository.save(customer);

		List<Event> events = client.loadAggregateRoot(id.toString());

		assertEquals(1, events.size());
		assertEquals(TestAggregateCreated.class.getSimpleName(), events.get(0).getEventType());
	}

  @Test
  public void deleteCreatesDeletedEvent() throws Exception {
    String id = UUID.randomUUID().toString();
    TestAggregate customer = new TestAggregate(id);
    repository.save(customer);

    repository.delete(customer);

    List<Event> events = client.loadAggregateRoot(id.toString());

    assertEquals(2, events.size());
    assertEquals(GenericAggregateDeletedEvent.class.getSimpleName(), events.get(1).getEventType());
  }

  @Test(expected = AggregateNotFoundException.class)
  public void loadDeletedAggregateThrowsException() throws Exception {
    String id = UUID.randomUUID().toString();
    TestAggregate customer = new TestAggregate(id);
    repository.save(customer);
    repository.delete(customer);

    repository.load(id);
  }

	@Test(expected = AggregateNotFoundException.class)
	public void throwsExceptionOnNonExistingAggregate() {
		repository.load("no-such-id-as-this");
	}

	@Test(expected = AggregateNotFoundException.class)
	public void withVersionThrowsExceptionOnNonExistingAggregate() {
		repository.load("no-such-id-as-this", 5L);
	}

	@Test
	public void canLoadWithVersion() {
    String id = "awesome-things";
		client.publishDomainEvents(id.toString(), Arrays.asList(
			new TestAggregateCreated(),
			new TestAggregateCreated()
		));

		TestAggregate aggregate = repository.load(id, 2L);
		assertEquals(2, aggregate.getVersion());
	}

	@Test(expected = OptimisticLockException.class)
	public void throwsOptimisticLocExceptionOnBadVersion() {
    String id = "awesome-id";
		client.publishDomainEvents(id.toString(), Arrays.asList(
			new TestAggregateCreated()
		));

		repository.load(id, 2L);
	}

  @Test()
  public void canStreamAggregateEvents() throws InterruptedException {
    String id = "cool-id";
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

    Thread.sleep(1500);

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
