package io.muoncore.newton.eventsource;

import io.muoncore.newton.NewtonEvent;
import lombok.Getter;
import lombok.Setter;

public abstract class TenantEvent<A> implements NewtonEvent {

  @Getter
  @Setter
  private String tenantId;

  public abstract A getId();

}
