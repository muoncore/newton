package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.NewtonIdentifier;

public interface SagaBus {

	<T extends Saga<P, ID>, P extends NewtonEvent, ID extends NewtonIdentifier> SagaMonitor<ID, T, P> dispatch(SagaIntent<ID, T, P> commandIntent);

}
