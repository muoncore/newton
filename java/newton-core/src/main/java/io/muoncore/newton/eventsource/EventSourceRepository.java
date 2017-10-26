package io.muoncore.newton.eventsource;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.NewtonEvent;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Allow loading and saving of event sourced aggregate roots.
 */
public interface EventSourceRepository<A extends AggregateRoot> {

  /**
   * Load an aggregate root from the event store. Fully replays all events into the aggregate before returning
   */
	A load(Object aggregateIdentifier) throws AggregateNotFoundException;
	CompletableFuture<A> loadAsync(Object aggregateIdentifier);

  /**
   * Load an aggregate root from the event store. Fully replays all events into the aggregate before returning
   * Enforces an optimistic lock. If the current version stored in the event store is different to the given version,
   * throws OptimisticLockException
   */
	A load(Object aggregateIdentifier, Long expectedVersion) throws AggregateNotFoundException, OptimisticLockException;
	CompletableFuture<A> loadAsync(Object aggregateIdentifier, Long expectedVersion) throws AggregateNotFoundException, OptimisticLockException;

  /**
   * Create a new instance of an aggregate via the given factory function.
   * Will persist and store
   */
	A newInstance(Callable<A> factoryMethod);

  /**
   * Persist an existing aggregate root.
   */
	List<NewtonEvent> save(A aggregate);

  /**
   * Delete this aggregate root. Its events may still be retrievable via replay, however a call to `load` will throw
   * AggregateNotFoundException
   * @param aggregate
   */
	List<NewtonEvent> delete(A aggregate);

  /**
   * Cold replay of the event contents of an aggregate root.
   */
	Publisher<NewtonEvent> replay(Object aggregateIdentifier);

  /**
   * Obtain notifications whenever the AR is updated.
   * This will first fully roll up the AR, emit the first AggregateRootUpdate with the current state of the AR, then whenever
   * new changes are made to the AR, a new AggregateRootUpdate will be emitted to subscribers.
   */
	Publisher<AggregateRootUpdate<A>> susbcribeAggregateUpdates(Object aggregateIdentifier);

  /**
   * Cold+hot replay of of the event contents of Aggregate.
   */
  Publisher<NewtonEvent> subscribeColdHot(Object aggregateIdentifier);

  /**
   * hot only subscription to the event contents of an Aggregate.
   * This publisher will emit when new data is added to this aggregate, but not any previously saved data.
   */
  Publisher<NewtonEvent> subscribeHot(Object aggregateIdentifier);
}
