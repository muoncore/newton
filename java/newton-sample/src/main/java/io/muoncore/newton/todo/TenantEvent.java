package io.muoncore.newton.todo;

import io.muoncore.newton.NewtonEvent;
import lombok.Getter;
import lombok.Setter;

public abstract class TenantEvent<A> implements NewtonEvent<A> {

  @Getter
  @Setter
  private String tenantId;
}
