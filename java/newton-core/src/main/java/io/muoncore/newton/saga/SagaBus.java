package io.muoncore.newton.saga;

public interface SagaBus {
	<T extends Saga> SagaMonitor<T> dispatch(SagaIntent<T> commandIntent);
}
