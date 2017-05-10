package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class SimpleSagaBus implements SagaBus {

	private SagaFactory sagaFactory;

	@Autowired
	public SimpleSagaBus(SagaFactory sagaFactory) {
		this.sagaFactory = sagaFactory;
	}

  @Override
  public <T extends Saga> SagaMonitor<T> dispatch(SagaIntent<T> commandIntent) {
		return sagaFactory.create(commandIntent.getType(), commandIntent.getPayload());
	}
}
