package io.muoncore.newton;

import io.muoncore.newton.eventsource.TenantEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * Base for multi tenant systems. All events emitted by this will contain tenancy information for consumption in views
 */
public class MultiTenantAggregate extends AggregateRoot {

    @Getter
    private String tenantId;

    protected MultiTenantAggregate(String tenantId) {
        raiseEvent(new MultiTenantAggregateCreatedEvent(tenantId));
    }

    @Override
    protected void raiseEvent(NewtonEvent event) {

        if (event instanceof TenantEvent) {
            ((TenantEvent) event).setTenantId(tenantId);
            super.raiseEvent(event);
            return;
        }
        if (event instanceof MultiTenantAggregateCreatedEvent) {
            super.raiseEvent(event);
            return;
        }
        throw new IllegalArgumentException("A MultiTenantAggregate must only emit TenantEvents. Type is not a TenantEvent: " + event.getClass().getName());
    }

    @OnDomainEvent
    public void on(MultiTenantAggregateCreatedEvent createdEvent) {
        this.tenantId = createdEvent.tenantId;
    }

    @Data
    @AllArgsConstructor
    public static class MultiTenantAggregateCreatedEvent implements NewtonEvent {
        private String tenantId;
    }
}
