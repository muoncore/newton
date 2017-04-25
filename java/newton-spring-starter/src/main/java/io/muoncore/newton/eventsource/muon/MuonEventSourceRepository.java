package io.muoncore.newton.eventsource.muon;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.DocumentId;
import io.muoncore.newton.eventsource.AggregateNotFoundException;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.AggregateEventClient;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.eventsource.EventSourceRepository;
import io.muoncore.newton.eventsource.OptimisticLockException;
import io.muoncore.newton.utils.muon.MuonLookupUtils;
import io.muoncore.protocol.event.client.EventReplayMode;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Slf4j
public class MuonEventSourceRepository<A extends AggregateRoot<DocumentId>> implements EventSourceRepository<A> {

	private Class<A> aggregateType;
	private AggregateEventClient aggregateEventClient;
	private EventClient eventClient;
	private final String boundedContextName;

	public MuonEventSourceRepository(Class<A> type, AggregateEventClient aggregateEventClient, EventClient eventClient, String boundedContextName) {
		aggregateType = type;
		this.aggregateEventClient = aggregateEventClient;
		this.eventClient = eventClient;
		this.boundedContextName = boundedContextName;
	}

	@Override
	public A load(DocumentId aggregateIdentifier) {
		try {
			A aggregate = aggregateType.newInstance();
			replayEvents(aggregateIdentifier).forEach(aggregate::handleEvent);
			return aggregate;
		} catch (AggregateNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to load aggregate: ".concat(aggregateType.getSimpleName()), e);
		}
	}

	@Override
	public A load(DocumentId aggregateIdentifier, Long version) {
		try {
			A aggregate = (A) aggregateType.newInstance();
			replayEvents(aggregateIdentifier).forEach(aggregate::handleEvent);
			if (aggregate.getVersion() != version) throw new OptimisticLockException(aggregateIdentifier, version, aggregate.getVersion());
			return aggregate;
		} catch (AggregateNotFoundException | OptimisticLockException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to load aggregate: ".concat(aggregateType.getSimpleName()), e);
		}
	}

	@Override
	public A newInstance(Callable<A> factoryMethod) {
		try {
			A result = factoryMethod.call();
			save(result);
			return result;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to create new instance: ".concat(aggregateType.getName()), e);
		}
	}

	@Override
	public void save(A aggregate) {
		emitForAggregatePersistence(aggregate);
		emitForStreamProcessing(aggregate);
	}

  @Override
  public void replay(DocumentId aggregateIdentifier, Subscriber<NewtonEvent> sub) {
    eventClient.replay("/aggregate/" + aggregateIdentifier.toString(), EventReplayMode.REPLAY_ONLY, new Subscriber<Event>() {
      public void onSubscribe(Subscription s) {
        sub.onSubscribe(s);
      }

      public void onNext(Event o) {
        log.warn("PROCESSING EVENT PLAY");
        sub.onNext(o.getPayload(MuonLookupUtils.getDomainClass(o)));
        log.warn("WOOT");
      }

      public void onError(Throwable t) {
        sub.onError(t);
      }

      public void onComplete() {
        sub.onComplete();
      }
    });
  }

  private List<NewtonEvent> replayEvents(DocumentId id) {
		try {
			List<NewtonEvent> events = aggregateEventClient.loadAggregateRoot(id.toString())
				.stream()
				.map(event -> event.getPayload(MuonLookupUtils.getDomainClass(event)))
				.collect(Collectors.toList());

			if (events.size() == 0) throw new AggregateNotFoundException(id);

			return events;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void emitForAggregatePersistence(A aggregate) {
		aggregateEventClient.publishDomainEvents(
			aggregate.getId().toString(),
			aggregate.getNewOperations());
	}

	private void emitForStreamProcessing(A aggregate) {
		String streamName = boundedContextName + "/" + aggregate.getClass().getSimpleName();
		log.debug("Emitting event on " + streamName);
		aggregate.getNewOperations().forEach(
			event -> eventClient.event(
				ClientEvent
					.ofType(event.getClass().getSimpleName())
					.stream(streamName)
					.payload(event)
					.build()
			));
	}
}
