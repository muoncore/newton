package io.muoncore.newton.eventsource;

import io.muoncore.newton.NewtonEvent;
import lombok.Getter;
import lombok.Setter;

public class TenantEvent implements NewtonEvent {
    @Getter
    @Setter
    private String tenantId;
}
