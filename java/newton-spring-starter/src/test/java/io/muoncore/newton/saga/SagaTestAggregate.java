package io.muoncore.newton.saga;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.AggregateRootId;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.eventsource.AggregateConfiguration;
import lombok.Data;

@Data
@AggregateConfiguration(context = "user")
public class SagaTestAggregate extends AggregateRoot<AggregateRootId> {

  public SagaTestAggregate() {
    raiseEvent(new SagaIntegrationTests.OrderRequestedEvent());
  }

  @EventHandler
  public void on(SagaIntegrationTests.OrderRequestedEvent ev) {
    setId(ev.getId());
  }
}