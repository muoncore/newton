package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.EventHandler;
import lombok.Data;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

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


    public static class TestSaga extends StatefulSaga {

        public MyEvent event;

        @StartSagaWith
        public void start(MyEvent event) {

        }

        @EventHandler
        public void onEvent(MyEvent myEvent) {
            event = myEvent;
        }

        public void onEvent(MyOtherEvent myEvent) {
        }
    }

    @Data
    static class MyEvent implements NewtonEvent {
      private final String id = "hello-world";
    }
    @Data
    static class MyOtherEvent implements NewtonEvent {
      private final String id = "hello-world";
    }
}
