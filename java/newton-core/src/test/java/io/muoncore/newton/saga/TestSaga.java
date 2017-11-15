package io.muoncore.newton.saga;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component
@SagaStreamConfig(aggregateRoots = SagaTestAggregate.class)
public class TestSaga extends StatefulSaga {
  @StartSagaWith
  public void start(SagaIntegrationTests.TriggerATestSagaEvent event) {
    end();
  }
}
