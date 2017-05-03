package io.muoncore.newton;

import lombok.Data;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vagrant on 21/03/17.
 */
public class DynamicInvokeEventAdaptorTest {
    @Test
    public void canInvokeClassHandler() throws Exception {
        Handler handler = new Handler();
        new DynamicInvokeEventAdaptor(handler, OnDomainEvent.class).accept(new Event1());
        assertTrue(handler.event instanceof Event1);
    }

    @Test
    public void canInvokeClassParentHandler() throws Exception {
        HandlerWithParent handler = new HandlerWithParent();
        new DynamicInvokeEventAdaptor(handler, OnDomainEvent.class).accept(new NonExplicitMatchEvent());
        assertTrue(handler.event instanceof NonExplicitMatchEvent);
    }

    @Test
    public void matchesAgainstParamInterfaceType() throws Exception {
        ParentHandler handler = new ParentHandler();
        new DynamicInvokeEventAdaptor(handler, OnDomainEvent.class).accept(new EventWithParent());
        assertTrue(handler.event instanceof EventWithParent);
    }

    public static class HandlerWithParent extends ParentHandler {

    }

    public static class Handler {
        NewtonEvent event;
        @OnDomainEvent
        public void on(Event1 ev) {this.event = ev;}
    }

    public static class ParentHandler {
        public NewtonEvent event;
        @OnDomainEvent
        public void on(NonExplicitMatchEvent ev) {this.event = ev;}
    }

    @Data
    static class Event1 implements NewtonEvent {
      private final AggregateRootId id = new SimpleAggregateRootId();
    }
    @Data
    static class ParentEvent implements NewtonEvent {
      private final AggregateRootId id = new SimpleAggregateRootId();
    }
    interface GenericEvent extends NewtonEvent {}
    @Data
    static class NonExplicitMatchEvent implements NewtonEvent {
      private final AggregateRootId id = new SimpleAggregateRootId();
    }
    @Data
    static class EventWithParent extends NonExplicitMatchEvent {
      private final AggregateRootId id = new SimpleAggregateRootId();
    }
}
