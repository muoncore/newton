package io.muoncore.newton.eventsource;

import io.muoncore.newton.AggregateRootId;
import io.muoncore.newton.NewtonEvent;
import lombok.Getter;
import lombok.Setter;

public class TenantEvent implements NewtonEvent {

  @Getter
  @Setter
  private AggregateRootId id;
  @Getter
  @Setter
  private String tenantId;

}
