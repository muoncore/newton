package io.muoncore.newton.saga;

import io.muoncore.newton.DocumentId;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.OnDomainEvent;
import lombok.Data;
import lombok.Getter;
import org.junit.Test;

import static org.junit.Assert.*;

public class StatefulSagaTest {
    @Test
    public void canDynamicDispatchEvents() throws Exception {
        TestSaga  saga = new TestSaga();

        saga.handle(new MyEvent());

        assertNotNull(saga.event);
    }
    @Test(expected = IllegalStateException.class)
    public void requiresAnnotation() throws Exception {
        TestSaga  saga = new TestSaga();

        saga.handle(new MyOtherEvent());

        assertNotNull(saga.event);
    }


    public static class TestSaga extends StatefulSaga<MyEvent> {

        public MyEvent event;

        @Override
        public void start(MyEvent event) {

        }

        @OnDomainEvent
        public void onEvent(MyEvent myEvent) {
            event = myEvent;
        }

        public void onEvent(MyOtherEvent myEvent) {
        }
    }

    @Data
    static class MyEvent implements NewtonEvent {
      private final DocumentId id = new DocumentId();
    }
    @Data
    static class MyOtherEvent implements NewtonEvent {
      private final DocumentId id = new DocumentId();
    }
}
