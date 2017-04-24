package io.muoncore.newton.saga;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.DocumentId;
import io.muoncore.newton.OnDomainEvent;
import io.muoncore.newton.eventsource.AggregateConfiguration;
import lombok.Data;

@Data
@AggregateConfiguration(context = "user")
public class SagaTestAggregate extends AggregateRoot<DocumentId> {

  public SagaTestAggregate() {
    raiseEvent(new SagaIntegrationTests.OrderRequestedEvent());
  }

  @OnDomainEvent
  public void on(SagaIntegrationTests.OrderRequestedEvent ev) {
    setId(ev.getId());
  }
}
