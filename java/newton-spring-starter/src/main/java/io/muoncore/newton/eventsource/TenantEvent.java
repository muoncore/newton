package io.muoncore.newton.eventsource;

import io.muoncore.newton.AggregateRootId;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.SimpleAggregateRootId;
import lombok.Getter;
import lombok.Setter;

public class TenantEvent implements NewtonEvent {

  @Getter
  private final AggregateRootId id = new SimpleAggregateRootId();

  @Getter
  @Setter
  private String tenantId;
}
