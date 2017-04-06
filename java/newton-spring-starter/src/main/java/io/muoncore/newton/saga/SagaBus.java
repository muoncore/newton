package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.DocumentId;

public interface SagaBus {

	<T extends Saga<P, ID>, P extends NewtonEvent, ID extends DocumentId> SagaMonitor<ID, T, P> dispatch(SagaIntent<ID, T, P> commandIntent);

}
