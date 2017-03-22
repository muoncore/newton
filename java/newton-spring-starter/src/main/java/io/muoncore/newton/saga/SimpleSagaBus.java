package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.NewtonIdentifier;
import org.springframework.beans.factory.annotation.Autowired;

public class SimpleSagaBus implements SagaBus {

	private SagaFactory sagaFactory;

	@Autowired
	public SimpleSagaBus(SagaFactory sagaFactory) {
		this.sagaFactory = sagaFactory;
	}

  @Override
  public <T extends Saga<P, ID>, P extends NewtonEvent, ID extends NewtonIdentifier> SagaMonitor<ID, T, P> dispatch(SagaIntent<ID, T, P> commandIntent) {
		return sagaFactory.create(commandIntent.getType(), commandIntent.getPayload());
	}
}
