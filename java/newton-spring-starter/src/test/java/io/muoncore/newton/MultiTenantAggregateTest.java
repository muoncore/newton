package io.muoncore.newton;

import io.muoncore.newton.eventsource.TenantEvent;
import org.junit.Test;

import static org.junit.Assert.*;

public class MultiTenantAggregateTest {
    @Test(expected = IllegalArgumentException.class)
    public void causesErrorWhenRaisingNonTenantEvent() throws Exception {
        new TestAggregate().wrong();
    }

    @Test
    public void raiseCreatedEvent() throws Exception {
        MultiTenantAggregate.MultiTenantAggregateCreatedEvent ev = (MultiTenantAggregate.MultiTenantAggregateCreatedEvent) new TestAggregate().getNewOperations().get(0);

        assertEquals("HELLO_TENANT", ev.getTenantId());
    }

    @Test
    public void copiesTenantIdIntoTenantEvents() throws Exception {
        TestAggregate ag = new TestAggregate();
        ag.doStuff();
        TenantEvent ev = (TenantEvent) ag.getNewOperations().get(1);

        assertEquals("HELLO_TENANT", ev.getTenantId());
    }

    static class TestAggregate extends MultiTenantAggregate {

        public TestAggregate() {
            super("HELLO_TENANT");
        }

        public void wrong() {
            raiseEvent(new WrongEvent());
        }
        public void doStuff() {
            raiseEvent(new GoodEvent());
        }
        @OnDomainEvent
        public void on(GoodEvent goodEvent) {}
    }

    static class WrongEvent implements NewtonEvent {}
    static class GoodEvent extends TenantEvent {}
}
