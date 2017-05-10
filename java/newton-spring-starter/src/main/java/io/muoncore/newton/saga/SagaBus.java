package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;

public interface SagaBus {

	<T extends Saga> SagaMonitor<T> dispatch(SagaIntent<T> commandIntent);

}
