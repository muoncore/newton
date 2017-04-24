package io.muoncore.newton.eventsource;

import io.muoncore.newton.DocumentId;
import io.muoncore.newton.NewtonEvent;
import lombok.Getter;
import lombok.Setter;

public class TenantEvent implements NewtonEvent {
  @Getter
  private final DocumentId id = new DocumentId();

  @Getter
    @Setter
    private String tenantId;
}
