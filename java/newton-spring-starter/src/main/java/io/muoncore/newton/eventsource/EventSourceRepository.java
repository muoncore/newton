package io.muoncore.newton.eventsource;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.DocumentId;
import io.muoncore.newton.NewtonEvent;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Callable;

/**
 * Allow loading and saving of event sourced aggregate roots.
 */
public interface EventSourceRepository<A extends AggregateRoot<DocumentId>> {

  /**
   * Load an aggregate root from the event store. Fully replays all events into the aggregate before returning
   */
	A load(DocumentId aggregateIdentifier) throws AggregateNotFoundException;

  /**
   * Load an aggregate root from the event store. Fully replays all events into the aggregate before returning
   * Enforces an optimistic lock. If the current version stored in the event store is different to the given version,
   * throws OptimisticLockException
   */
	A load(DocumentId aggregateIdentifier, Long expectedVersion) throws AggregateNotFoundException, OptimisticLockException;

  /**
   * Create a new instance of an aggregate via the given factory function.
   * Will persist and store
   */
	A newInstance(Callable<A> factoryMethod);

  /**
   * Persist an existing aggregate root.
   */
	void save(A aggregate);

  /**
   * Cold replay of the event contents of an aggregate root.
   */
	Publisher<NewtonEvent> replay(DocumentId aggregateIdentifier);

  /**
   * Cold+hot replay of of the event contents of Aggregate.
   */
  Publisher<NewtonEvent> subscribeColdHot(DocumentId aggregateIdentifier);

  /**
   * hot only subscription to the event contents of an Aggregate.
   * This publisher will emit when new data is added to this aggregate, but not any previously saved data.
   */
  Publisher<NewtonEvent> subscribeHot(DocumentId aggregateIdentifier);
}
