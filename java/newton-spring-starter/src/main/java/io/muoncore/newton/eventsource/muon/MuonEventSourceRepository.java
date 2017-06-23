package io.muoncore.newton.eventsource.muon;

import io.muoncore.newton.AggregateEventClient;
import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.eventsource.*;
import io.muoncore.newton.utils.muon.MuonLookupUtils;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventReplayMode;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Slf4j
public class MuonEventSourceRepository<A extends AggregateRoot> implements EventSourceRepository<A> {

	private Class<A> aggregateType;
	private AggregateEventClient aggregateEventClient;
	private EventClient eventClient;
	private EventStreamProcessor processor;
	private String streamName;

	public MuonEventSourceRepository(Class<A> type,
                                   AggregateEventClient aggregateEventClient,
                                   EventClient eventClient,
                                   EventStreamProcessor eventStreamProcessor, String appName) {
		aggregateType = type;
		this.processor = eventStreamProcessor;
		this.aggregateEventClient = aggregateEventClient;
		this.eventClient = eventClient;
		this.streamName = AggregateRootUtil.getAggregateRootStream(type, appName);
	}

	@Override
	public A load(Object aggregateIdentifier) {
		try {
			A aggregate = aggregateType.newInstance();
			replayEvents(aggregateIdentifier).forEach(aggregate::handleEvent);
			if (aggregate.isDeleted()) throw new AggregateNotFoundException(aggregateIdentifier);
			return aggregate;
		} catch (AggregateNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to load aggregate: ".concat(aggregateType.getSimpleName()), e);
		}
	}

	@Override
	public A load(Object aggregateIdentifier, Long version) {
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
	public List<NewtonEvent> save(A aggregate) {
		emitForAggregatePersistence(aggregate);
		emitForStreamProcessing(aggregate);
		List<NewtonEvent> events = new ArrayList<>(aggregate.getNewOperations());
		aggregate.getNewOperations().clear();
		return events;
	}

  @Override
  public List<NewtonEvent> delete(A aggregate) {
    aggregate.delete();
    return save(aggregate);
  }

  private Publisher<NewtonEvent> subscribe(Object aggregateIdentifier, EventReplayMode mode) {
	  return sub -> eventClient.replay("/aggregate/" + aggregateType.getSimpleName() + "/" + aggregateIdentifier.toString(), mode, new Subscriber<Event>() {
        public void onSubscribe(Subscription s) {
          sub.onSubscribe(s);
        }

        public void onNext(Event o) {
          sub.onNext(o.getPayload(MuonLookupUtils.getDomainClass(o)));
        }

        public void onError(Throwable t) {
          sub.onError(t);
        }

        public void onComplete() {
          sub.onComplete();
        }
      });
  }

  @Override
  public Publisher<NewtonEvent> replay(Object aggregateIdentifier) {
	  return subscribe(aggregateIdentifier, EventReplayMode.REPLAY_ONLY);
  }

  @Override
  public Publisher<NewtonEvent> subscribeColdHot(Object aggregateIdentifier) {
    return subscribe(aggregateIdentifier, EventReplayMode.REPLAY_THEN_LIVE);
  }

  @Override
  public Publisher<NewtonEvent> subscribeHot(Object aggregateIdentifier) {
    return subscribe(aggregateIdentifier, EventReplayMode.LIVE_ONLY);
  }

  private List<NewtonEvent> replayEvents(Object id) {
		try {
			List<NewtonEvent> events = aggregateEventClient.loadAggregateRoot(id.toString(), aggregateType)
				.stream()
				.map(event -> {
          Class<? extends NewtonEvent> domainClass = MuonLookupUtils.getDomainClass(event);
          if (domainClass == null) {
            log.error("Unable to load event {} for domain class {}", event.getEventType(), this.aggregateType);
            throw new IllegalStateException("Unable to load aggregate with id " + id + " as event type " + event.getEventType() + " could not be found");
          }
          return event.getPayload(domainClass);
        })
				.collect(Collectors.toList());

			if (events.size() == 0) throw new AggregateNotFoundException(id);

			return (List<NewtonEvent>) processor.processForLoad(events);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void emitForAggregatePersistence(A aggregate) {
		aggregateEventClient.publishDomainEvents(
			aggregate.getId().toString(),
      aggregateType,
      processor.processForPersistence(aggregate.getNewOperations()));
	}

	private void emitForStreamProcessing(A aggregate) {
		log.debug("Emitting event on " + streamName);
    processor.processForPersistence(aggregate.getNewOperations()).forEach(
			event -> eventClient.event(
				ClientEvent
					.ofType(event.getClass().getSimpleName())
					.stream(streamName)
					.payload(event)
					.build()
			));
	}
}
