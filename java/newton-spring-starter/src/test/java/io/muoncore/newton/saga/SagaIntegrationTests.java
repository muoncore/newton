package io.muoncore.newton.saga;

import io.muoncore.newton.*;
import io.muoncore.newton.command.Command;
import io.muoncore.newton.command.CommandBus;
import io.muoncore.newton.command.CommandConfiguration;
import io.muoncore.newton.command.CommandIntent;
import io.muoncore.newton.eventsource.EventSourceRepository;
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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ActiveProfiles({"test", "log-events"})
@ContextConfiguration(classes = {QueryConfiguration.class, CommandConfiguration.class, MuonTestConfiguration.class, SagaIntegrationTests.class, SagaConfiguration.class, MongoConfiguration.class})
@RunWith(SpringRunner.class)
@Configuration
@SpringBootTest
@EnableNewton("io.muoncore.newton.saga")
@ComponentScan
public class SagaIntegrationTests {

  @Autowired
  private SagaFactory sagaFactory;
  @Autowired
  private SagaBus sagaBus;
  @Autowired
  private SagaRepository sagaRepository;
  @Autowired
  private CommandBus commandBus;

  @Autowired
  private EventSourceRepository<SagaTestAggregate> testAggregateRepo;

  @Test
  public void sagaCanBeStartedViaStartEvent() throws InterruptedException {

    //save a domain class, triggering a save event
    NewtonEvent<String> createEvent = testAggregateRepo.save(new SagaTestAggregate()).get(0);

    Thread.sleep(1000);
    //lookup the saga via the ID
    List<SagaCreated> sagas = sagaRepository.getSagasCreatedByEventId(createEvent.getId());
    SagaMonitor<ComplexSaga> monitor = sagaFactory.monitor(sagas.get(0).getSagaId(), ComplexSaga.class);

    ComplexSaga saga = monitor.waitForCompletion(TimeUnit.SECONDS, 1);

    assertTrue(saga.isComplete());
  }

  @Test
  public void sagaCanBeStartedViaIntent() throws InterruptedException {

    TestSaga testSaga = sagaBus.dispatch(
      new SagaIntent<>(TestSaga.class, new OrderRequestedEvent())).waitForCompletion(TimeUnit.MINUTES, 1);

    assertTrue(testSaga.isComplete());
  }

  @Test
  public void sagaCanBeLoadedLater() {
    SagaMonitor<TestSaga> sagaMonitor = sagaBus.dispatch(
      new SagaIntent<>(TestSaga.class, new OrderRequestedEvent()));

    Optional<TestSaga> load = sagaRepository.load(sagaMonitor.getId(), TestSaga.class);

    assertTrue(load.isPresent());

  }

  @Test
  public void sagaCanBeMonitoredLater() {
    SagaMonitor<TestSaga> sagaMonitor = sagaBus.dispatch(
      new SagaIntent<>(TestSaga.class, new OrderRequestedEvent()));

    SagaMonitor<TestSaga> monitor = sagaFactory.monitor(sagaMonitor.getId(), TestSaga.class);

    assertNotNull(monitor);

  }

  @Test
  public void multiStepSagaWorkflow() {

    SagaMonitor<ComplexSaga> sagaMonitor = sagaBus.dispatch(
      new SagaIntent<>(ComplexSaga.class, new OrderRequestedEvent()));

    ComplexSaga saga = sagaMonitor.waitForCompletion(TimeUnit.MINUTES, 1);

    assertNotNull(saga);
    assertTrue(saga.isComplete());
  }

  @Scope("prototype")
  @Component
  @SagaStreamConfig(streams = {"TestAggregate"}, aggregateRoots = {SagaTestAggregate.class})
  public static class ComplexSaga extends StatefulSaga {

    private String orderId;

    @StartSagaWith
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

    @EventHandler
    public void on(PaymentRecievedEvent payment) {
      System.out.println("Payment received, ordering shipping... ");
      //order a shipping
      raiseCommand(CommandIntent.builder(ShipOrder.class.getName())
        .id(orderId)
        .build()
      );
    }

    @EventHandler
    public void on(OrderShippedEvent shippedEvent) {
      System.out.println("Order shipped, saga is completed ... ");
      end();
    }
  }

  @Getter
  public static class OrderRequestedEvent implements NewtonEvent {
    private final String id = UUID.randomUUID().toString();
  }

  @Getter
  @AllArgsConstructor
  @ToString
  public static class PaymentRecievedEvent implements NewtonEvent {
    private String orderId;
    private final String id = UUID.randomUUID().toString();
  }

  @Getter
  @AllArgsConstructor
  @ToString
  public static class OrderShippedEvent implements NewtonEvent {
    private String orderId;
    private final String id = UUID.randomUUID().toString();
  }

  @Scope("prototype")
  @Component
  public static class TakePayment implements Command {

    @Autowired
    private EventClient eventClient;
    private String orderId;

    public void setId(String id) {
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
  public static class ShipOrder implements Command {

    @Autowired
    private EventClient eventClient;
    private String orderId;

    public void setId(String id) {
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
