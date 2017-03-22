package io.muoncore.newton.eventsource;

import io.muoncore.newton.NewtonIdentifier;

import java.util.concurrent.Callable;

public interface EventSourceRepository<A> {

	A load(NewtonIdentifier aggregateIdentifier) throws AggregateNotFoundException;

	A load(NewtonIdentifier aggregateIdentifier, Long expectedVersion) throws AggregateNotFoundException, OptimisticLockException;

	A newInstance(Callable<A> factoryMethod);

	void save(A aggregate);

}
