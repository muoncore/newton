package io.muoncore.newton.saga;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component
@SagaStreamConfig(streams="user/SagaTestAggregate")
public class TestSaga extends StatefulSaga<SagaIntegrationTests.OrderRequestedEvent> {
  @Override
  public void start(SagaIntegrationTests.OrderRequestedEvent event) {
    end();
  }
}
