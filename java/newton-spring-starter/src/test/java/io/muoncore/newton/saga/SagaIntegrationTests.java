package io.muoncore.newton.saga;

import io.muoncore.newton.*;
import io.muoncore.newton.command.CommandConfiguration;
import io.muoncore.newton.command.CommandIntent;
import io.muoncore.newton.command.IdentifiableCommand;
import io.muoncore.newton.eventsource.muon.TestAggregate;
import io.muoncore.newton.mongo.MongoConfiguration;
import io.muoncore.newton.query.QueryConfiguration;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ActiveProfiles({"test", "log-events"})
@ContextConfiguration(classes = {QueryConfiguration.class, CommandConfiguration.class, MuonTestConfiguration.class, SagaIntegrationTests.class, SagaConfiguration.class, MongoConfiguration.class})
@RunWith(SpringRunner.class)
@Configuration
@SpringBootTest
public class SagaIntegrationTests {

    @Autowired
    private SagaFactory sagaFactory;
    @Autowired
    private SagaBus sagaBus;
    @Autowired
    private SagaRepository sagaRepository;

    @Test
    public void sagaCanBeStartedViaIntent() throws InterruptedException {

        TestSaga testSaga = sagaBus.dispatch(
                new SagaIntent<>(TestSaga.class, new OrderRequestedEvent())).waitForCompletion(TimeUnit.MINUTES, 1);

        assertTrue(testSaga.isComplete());
    }

    @Test
    public void sagaCanBeLoadedLater() {
        SagaMonitor<DocumentId, TestSaga, OrderRequestedEvent> sagaMonitor = sagaBus.dispatch(
                new SagaIntent<>(TestSaga.class, new OrderRequestedEvent()));

        Optional<TestSaga> load = sagaRepository.load(sagaMonitor.getId(), TestSaga.class);

        assertTrue(load.isPresent());

    }

    @Test
    public void sagaCanBeMonitoredLater() {
        SagaMonitor<DocumentId, TestSaga, OrderRequestedEvent> sagaMonitor = sagaBus.dispatch(
                new SagaIntent<>(TestSaga.class, new OrderRequestedEvent()));

        SagaMonitor<DocumentId, TestSaga, OrderRequestedEvent> monitor = sagaFactory.monitor(sagaMonitor.getId(), TestSaga.class);

        assertNotNull(monitor);

    }

    @Test
    public void multiStepSagaWorkflow() {

        SagaMonitor<DocumentId, ComplexSaga, OrderRequestedEvent> sagaMonitor = sagaBus.dispatch(
                new SagaIntent<>(ComplexSaga.class, new OrderRequestedEvent()));

        ComplexSaga saga = sagaMonitor.waitForCompletion(TimeUnit.MINUTES, 1);

        assertNotNull(saga);
        assertTrue(saga.isComplete());
    }

    @Scope("prototype")
    @Component
    public class TestSaga extends StatefulSaga<OrderRequestedEvent> {
        @Override
        public void start(OrderRequestedEvent event) {
            end();
        }
    }

    @Scope("prototype")
    @Component
    @SagaStreamConfig(streams = {"TestAggregate"})
    public class ComplexSaga extends StatefulSaga<OrderRequestedEvent> {

        private DocumentId orderId;

        @Override
        public void start(OrderRequestedEvent event) {
            orderId = event.getId();

            System.out.println("Got order id  " + orderId);

            notifyOn(PaymentRecievedEvent.class, "orderId", orderId.toString());
            notifyOn(OrderShippedEvent.class, "orderId", orderId.toString());

            raiseCommand(CommandIntent.builder(TakePayment.class.getName())
                    .id(orderId)
                    .build()
            );
            System.out.println("Saga is started ... ");
        }

        @OnDomainEvent
        public void on(PaymentRecievedEvent payment) {
            System.out.println("Payment received, ordering shipping... ");
            //order a shipping
            raiseCommand(CommandIntent.builder(ShipOrder.class.getName())
                    .id(orderId)
                    .build()
            );
        }

        @OnDomainEvent
        public void on(OrderShippedEvent shippedEvent) {
            System.out.println("Order shipped, saga is completed ... ");
            end();
        }
    }

    @Getter
    public static class OrderRequestedEvent implements NewtonEvent {
        private DocumentId id = new DocumentId();
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class PaymentRecievedEvent implements NewtonEvent {
        private DocumentId orderId;
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class OrderShippedEvent implements NewtonEvent {
        private DocumentId orderId;
    }

    @Scope("prototype")
    @Component
    static class TakePayment implements IdentifiableCommand<DocumentId> {

        @Autowired
        private EventClient eventClient;
        private DocumentId orderId;

        @Override
        public void setId(DocumentId id) {
            this.orderId = id;
        }

        @Override
        public void execute() {

            //fake interaction with an aggregate and dump out domain events directly.
            EventResult event = eventClient.event(
                    ClientEvent
                            .ofType(PaymentRecievedEvent.class.getSimpleName())
                            .stream(TestAggregate.class.getSimpleName())
                            .payload(new PaymentRecievedEvent(orderId))
                            .build()
            );

            System.out.println("NewtonEvent result is " + event);
        }
    }

    @Scope("prototype")
    @Component
    @Slf4j
    static class ShipOrder implements IdentifiableCommand<DocumentId> {

        @Autowired
        private EventClient eventClient;
        private DocumentId orderId;

        @Override
        public void setId(DocumentId id) {
            this.orderId = id;
        }

        @Override
        public void execute() {
            log.info("Pretending to do some fulfillment ... ");
            //fake interaction with an aggregate and dump out domain events directly.
            EventResult result = eventClient.event(
                    ClientEvent
                            .ofType(OrderShippedEvent.class.getSimpleName())
                            .stream(TestAggregate.class.getSimpleName())
                            .payload(new OrderShippedEvent(orderId))
                            .build()
            );
            log.info("Got result " + result);
        }
    }
}
