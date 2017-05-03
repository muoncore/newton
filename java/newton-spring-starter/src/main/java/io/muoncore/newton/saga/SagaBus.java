package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.AggregateRootId;

public interface SagaBus {

	<T extends Saga<P, ID>, P extends NewtonEvent, ID extends AggregateRootId> SagaMonitor<ID, T> dispatch(SagaIntent<ID, T, P> commandIntent);

}
