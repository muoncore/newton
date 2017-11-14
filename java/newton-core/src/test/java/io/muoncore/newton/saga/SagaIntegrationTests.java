package io.muoncore.newton.saga;

import io.muoncore.newton.EnableNewton;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.InMemoryTestConfiguration;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.command.*;
import io.muoncore.newton.eventsource.EventSourceRepository;
import io.muoncore.newton.eventsource.muon.TestAggregate;
import io.muoncore.newton.query.InMemoryQueryConfiguration;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
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

import static org.junit.Assert.*;

@ActiveProfiles({"test", "log-events"})
@ContextConfiguration(
  classes = {
    InMemoryQueryConfiguration.class,
    CommandConfiguration.class,
    TestSagaConfiguration.class,
    InMemoryTestConfiguration.class,
  })
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
  private EventClient eventClient;

  @Autowired
  private EventSourceRepository<SagaTestAggregate> testAggregateRepo;

  @Test
  public void sagaCommandFailureCallsFailedEventHandler() throws InterruptedException {
    FailAThingEvent failAThingEvent = new FailAThingEvent();

    eventClient.event(ClientEvent.ofType(FailAThingEvent.class.getSimpleName())
                                 .payload(failAThingEvent)
                                 .stream("faked")
                                 .build()
    );

    Thread.sleep(1000);
    //lookup the saga via the ID
    List<SagaCreated> sagas = sagaRepository.getSagasCreatedByEventId(failAThingEvent.getId());
    SagaMonitor<FailCommandSaga> monitor = sagaFactory.monitor(sagas.get(0).getSagaId(), FailCommandSaga.class);

    FailCommandSaga saga = monitor.waitForCompletion(TimeUnit.SECONDS, 1);

    Optional<FailCommandSaga> load = sagaRepository.load(saga.getId(), FailCommandSaga.class);

    System.out.println("SAGAS =" + sagas);
    assertEquals(1, sagas.size());
//    assertTrue(saga.isComplete());
    assertNotNull(load.get().getFailresult());
    assertTrue(load.get().getFailresult().contains("Broken!"));
  }

  @Test
  public void sagaCanBeStartedViaStartEvent() throws InterruptedException {
    // Save a domain class, triggering a save event
    NewtonEvent<String> createEvent = testAggregateRepo.save(new SagaTestAggregate()).get(0);

    Thread.sleep(1000);

    // Lookup the saga via the ID
    List<SagaCreated> sagas = sagaRepository.getSagasCreatedByEventId(createEvent.getId());
    SagaMonitor<ComplexSaga> monitor = sagaFactory.monitor(sagas.get(0).getSagaId(), ComplexSaga.class);

    ComplexSaga saga = monitor.waitForCompletion(TimeUnit.SECONDS, 1);

    System.out.println("SAGAS = " + sagas);
    assertTrue(saga.isComplete());
  }

  @Test
  public void sagaCanBeStartedViaIntent() throws InterruptedException {

    TestSaga testSaga = sagaBus.dispatch(
      new SagaIntent<>(TestSaga.class, new TriggerATestSagaEvent())).waitForCompletion(TimeUnit.MINUTES, 1);

    assertTrue(testSaga.isComplete());
  }

  @Test
  public void sagaCanBeLoadedLater() {
    SagaMonitor<TestSaga> sagaMonitor = sagaBus.dispatch(
      new SagaIntent<>(TestSaga.class, new TriggerATestSagaEvent()));

    Optional<TestSaga> load = sagaRepository.load(sagaMonitor.getId(), TestSaga.class);

    assertTrue(load.isPresent());

  }

  @Test
  public void sagaCanBeMonitoredLater() {
    SagaMonitor<TestSaga> sagaMonitor = sagaBus.dispatch(
      new SagaIntent<>(TestSaga.class, new TriggerATestSagaEvent()));

    SagaMonitor<TestSaga> monitor = sagaFactory.monitor(sagaMonitor.getId(), TestSaga.class);

    assertNotNull(monitor);

  }

  @Test
  public void multiStepSagaWorkflow() throws InterruptedException {
    SagaMonitor<ComplexSaga> sagaMonitor = sagaBus.dispatch(
      new SagaIntent<>(ComplexSaga.class, new OrderRequestedEvent())
    );

    ComplexSaga saga = sagaMonitor.waitForCompletion(TimeUnit.MINUTES, 1);

    saga = sagaRepository.load(saga.id, ComplexSaga.class).get();

    assertNotNull(saga);
    assertTrue(saga.isComplete());
  }

  @Scope("prototype")
  @Component
  @SagaStreamConfig(streams = {"TestAggregate"}, aggregateRoots = {SagaTestAggregate.class})
  public static class ComplexSaga extends StatefulSaga {

    private String orderId;
    @Autowired
    private ApplicationContext context;

    @StartSagaWith
    public void start(OrderRequestedEvent event) {
      orderId = event.getId();

      System.out.println("App name is " + context.getApplicationName());

      System.out.println("Got order id  " + orderId);

      notifyOn(PaymentReceivedEvent.class, "orderId", orderId.toString());
      notifyOn(OrderShippedEvent.class, "orderId", orderId.toString());

      raiseCommand(CommandIntent.builder(TakePayment.class.getName())
                                .id(orderId)
                                .build()
      );
      System.out.println("Saga is started ... " + id);
    }

    @EventHandler
    public void on(PaymentReceivedEvent payment) {
      System.out.println("App name is " + context.getApplicationName());
      System.out.println("Payment received, ordering shipping... ");
      //order a shipping
      raiseCommand(CommandIntent.builder(ShipOrder.class.getName())
                                .id(orderId)
                                .build()
      );
    }

    @EventHandler
    public void on(OrderShippedEvent shippedEvent) {
      System.out.println("Order shipped, saga is completed ... " + id);
      end();
    }
  }

  @Getter
  public static class OrderRequestedEvent implements NewtonEvent {
    private final String id = UUID.randomUUID().toString();
  }

  @Getter
  @ToString
  public static class PaymentReceivedEvent implements NewtonEvent {
    private String orderId;
    private final String id = UUID.randomUUID().toString();

    public PaymentReceivedEvent(String orderId) {
      this.orderId = orderId;
    }
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
          .ofType(PaymentReceivedEvent.class.getSimpleName())
          .stream(TestAggregate.class.getSimpleName())
          .payload(new PaymentReceivedEvent(orderId))
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

  @Scope("prototype")
  @Component
  @SagaStreamConfig(streams = {"faked"}, aggregateRoots = {})
  public static class FailCommandSaga extends StatefulSaga {
    @Getter @Setter
    private String dataName = "hello!";
    @Getter @Setter
    private String failresult;

    @StartSagaWith
    public void start(FailAThingEvent event) {
      raiseCommand(CommandIntent.builder(FailingCommand.class.getName())
                                .build()
      );
    }

    @EventHandler
    public void on(CommandFailedEvent payment) {
      this.failresult = payment.getFailureMessage();
      System.out.println("Command has failed and the saga will be terminated.");
      end();
    }
  }

  @Getter
  @ToString
  public static class FailAThingEvent implements NewtonEvent {
    private final String id = UUID.randomUUID().toString();
  }

  @Scope("prototype")
  @Component
  @Slf4j
  public static class FailingCommand implements Command {
    @Override
    public void execute() {
      log.info("Now failing!");
      throw new IllegalStateException("Broken!");
    }
  }

  @Getter
  @ToString
  public static class TriggerATestSagaEvent implements NewtonEvent {
    private final String id = UUID.randomUUID().toString();
  }

}
