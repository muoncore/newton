package io.muoncore.newton.saga;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.eventsource.AggregateConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AggregateConfiguration(context = "user")
public class MongoSagaTestAggregate extends AggregateRoot<String> {

  private String id;

  public MongoSagaTestAggregate() {
    raiseEvent(new MongoSagaIntegrationTests.OrderRequestedEvent());
  }

  @EventHandler
  public void on(MongoSagaIntegrationTests.OrderRequestedEvent ev) {
    setId(ev.getId());
  }
}
