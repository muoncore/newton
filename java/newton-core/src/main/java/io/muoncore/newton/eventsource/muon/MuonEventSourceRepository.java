package io.muoncore.newton.eventsource.muon;

import io.muoncore.exception.MuonException;
import io.muoncore.newton.*;
import io.muoncore.newton.eventsource.*;
import io.muoncore.newton.utils.muon.MuonLookupUtils;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.EventBuilder;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventReplayMode;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class MuonEventSourceRepository<A extends AggregateRoot> implements EventSourceRepository<A> {

	private Class<A> aggregateType;
	private NewtonEventClient eventClient;
	private EventStreamProcessor processor;
	private String streamName;
	private AggregateRootSnapshotRepository snapshotRepository;

	public MuonEventSourceRepository(Class<A> type,
                                   AggregateRootSnapshotRepository snapshotRepository,
                                   NewtonEventClient aggregateEventClient,
                                   EventStreamProcessor eventStreamProcessor, String appName) {
		aggregateType = type;
		this.snapshotRepository = snapshotRepository;
		this.processor = eventStreamProcessor;
		this.eventClient = aggregateEventClient;
		this.streamName = AggregateRootUtil.getAggregateRootStream(type, appName);
	}

  @Override
  public CompletableFuture<A> loadAsync(Object aggregateIdentifier) {
    return CompletableFuture.supplyAsync(() -> load(aggregateIdentifier));
  }

  @Override
  public CompletableFuture<A> loadAsync(Object aggregateIdentifier, Long expectedVersion) throws AggregateNotFoundException, OptimisticLockException {
    return CompletableFuture.supplyAsync(() -> load(aggregateIdentifier, expectedVersion));
  }

  @Override
	public A load(Object aggregateIdentifier) {
		try {
			AggregateRoot aggregate = snapshotRepository.getLatestSnapshot(aggregateIdentifier).orElseGet(() -> {
        try {
          AggregateRoot ag = aggregateType.newInstance();
          replayEvents(aggregateIdentifier).forEach(ag::handleEvent);
          return ag;
        } catch (InstantiationException | IllegalAccessException e) {
          throw new MuonException("Error creating new instance of Aggregate", e);
        }
      });

      if (aggregate.isDeleted()) throw new AggregateNotFoundException(aggregateIdentifier);
			return (A) aggregate;
		} catch (EventStoreException | AggregateNotFoundException e) {
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
		} catch (EventStoreException | AggregateNotFoundException | OptimisticLockException e) {
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
		snapshotRepository.persist(aggregate);
		return events;
	}

  @Override
  public List<NewtonEvent> delete(A aggregate) {
    aggregate.delete();
    return save(aggregate);
  }

  private Publisher<NewtonEvent> subscribe(Object aggregateIdentifier, EventReplayMode mode) {
	  return sub -> eventClient.replay(createAggregateStreamName(aggregateIdentifier), mode, new Subscriber<Event>() {
        public void onSubscribe(Subscription s) {
          sub.onSubscribe(s);
        }

        public void onNext(Event o) {
          sub.onNext(MuonLookupUtils.decorateMeta(o.getPayload(MuonLookupUtils.getDomainClass(o)), o));
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
  public Publisher<AggregateRootUpdate<A>> susbcribeAggregateUpdates(Object aggregateIdentifier) {

	  A root = load(aggregateIdentifier);

    //should capture the order-id played up to and request from there.
    return sub -> eventClient.replay(createAggregateStreamName(aggregateIdentifier), EventReplayMode.LIVE_ONLY, new Subscriber<Event>() {
      public void onSubscribe(Subscription s) {
        sub.onSubscribe(s);
        sub.onNext(new AggregateRootUpdate<>(root, null));
      }

      public void onNext(Event o) {
        NewtonEvent payload = MuonLookupUtils.decorateMeta(o.getPayload(MuonLookupUtils.getDomainClass(o)), o);
        root.handleEvent(payload);
        sub.onNext(new AggregateRootUpdate<>(root, payload));
      }

      public void onError(Throwable t) {
        sub.onError(t);
      }

      public void onComplete() {
        sub.onComplete();
      }
    });
  }

  private String createAggregateStreamName(Object aggregateIdentifier) {
    return eventClient.createAggregateStreamName(aggregateIdentifier.toString(), aggregateType);
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
			List<NewtonEvent> events = eventClient.loadAggregateRoot(id.toString(), aggregateType).get()
				.stream()
				.map(event -> {
          Class<? extends NewtonEvent> domainClass = MuonLookupUtils.getDomainClass(event);
          if (domainClass == null) {
            log.error("Unable to load event {} for domain class {}", event.getEventType(), this.aggregateType);
            throw new IllegalStateException("Unable to load aggregate with id " + id + " as event type " + event.getEventType() + " could not be found");
          }
          return MuonLookupUtils.decorateMeta(event.getPayload(domainClass), event);
        })
				.collect(Collectors.toList());

			if (events.size() == 0) throw new AggregateNotFoundException(id);

			return (List<NewtonEvent>) processor.processForLoad(events);
		} catch (ExecutionException e) {
		  if (e.getCause() instanceof RuntimeException) {
		    throw (RuntimeException) e.getCause();
      }
		  throw new RuntimeException(e.getCause());
    } catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
  }

	private void emitForAggregatePersistence(A aggregate) {
		eventClient.publishDomainEvents(
			aggregate.getId().toString(),
      aggregateType,
      processor.processForPersistence(aggregate.getNewOperations()), cause.get());
	}

	private void emitForStreamProcessing(A aggregate) {
    processor.processForPersistence(aggregate.getNewOperations()).forEach(
			event -> {
        log.debug("Emitting {} event on {}", event, streamName);

        EventBuilder payload = ClientEvent
          .ofType(event.getClass().getSimpleName())
          .id(aggregate.getId().toString())
          .stream(streamName)
          .payload(event);

        if (cause.get() != null) {
          payload.causedBy(String.valueOf(cause.get().getMeta().getOrderId()), "CAUSED");
        }

        ClientEvent ev = payload.build();

        eventClient.event(ev);
      });
	}

	// manage causation between events.
	private static ThreadLocal<NewtonEventWithMeta> cause = new ThreadLocal<>();

	public static <T> T executeCausedBy(NewtonEvent ev, Supplier<T> run) {
	  if (NewtonEventWithMeta.class.isAssignableFrom(ev.getClass())) {
      cause.set((NewtonEventWithMeta) ev);
    }
	  T ret = run.get();
	  cause.remove();
	  return ret;
  }
}
