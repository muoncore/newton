package todo

import io.muoncore.newton.EventHandler
import io.muoncore.newton.NewtonEvent
import io.muoncore.newton.SampleApplication
import io.muoncore.newton.StreamSubscriptionManager
import io.muoncore.newton.TodoSaga
import io.muoncore.newton.domainservice.EventDrivenDomainService
import io.muoncore.newton.eventsource.EventTypeNotFound
import io.muoncore.newton.eventsource.muon.MuonEventSourceRepository
import io.muoncore.newton.saga.SagaFactory
import io.muoncore.newton.saga.SagaRepository
import io.muoncore.newton.support.DocumentId
import io.muoncore.newton.support.TenantContextHolder
import io.muoncore.newton.todo.Task
import io.muoncore.newton.todo.TenantEvent
import io.muoncore.protocol.event.ClientEvent
import io.muoncore.protocol.event.client.EventClient
import org.junit.Ignore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

@ActiveProfiles(["test", "functional"])
@SpringBootTest(classes = [MuonTestConfig, SampleApplication, FailConfig])
class MissingEventSpec extends Specification {

  @Autowired
  MyEventService eventService

  @Autowired
  EventClient eventClient

  @Autowired
  MuonEventSourceRepository<Task> repo

  @Autowired
  SagaRepository sagaRepository

  def "can replay aggregate streams in parallel"() {

    Task task = repo.newInstance { new Task(new DocumentId(), "Hi!") }


    sleep 1000
    when:

    def num = new AtomicInteger(1)

    def latch = new CountDownLatch(40)

    20.times {

      def exec = {
        try {
          TenantContextHolder.setTenantId("hello")
          Task t = repo.load(task.id)
          t.changeDescription("Hello" + num.addAndGet(1))
          repo.save(t)
          TenantContextHolder.setTenantId(null)
        } finally {
          latch.countDown()
          println "COUNTDOWN $num"
        }
      }

      exec()
      exec()
//      Thread.start exec
//      Thread.start exec
    }

    latch.await()
//    sleep(500)

    task = repo.load(task.id)

    then:
    task.description == "Hello41"
  }

  def "event service can emit events"() {
    when:
    println "EMIT WAS " + eventClient.event(ClientEvent.ofType(FirstEvent.simpleName).stream("newstream").payload(new FirstEvent(id:"12345")).build()).status

    then:
    new PollingConditions().eventually {
      eventService.ev2?.id
    }
  }

  def "can start a saga via an event"() {
    when:
    def events = repo.save(new Task(new DocumentId(), "Hi!"))

    println "Event is $events"
    sleep(500)
    def sagas = sagaRepository.getSagasCreatedByEventId(events[0].id)

    then:
    sagas.size() == 1
    sagas[0] instanceof TodoSaga
  }
}


@Configuration
class FailConfig {

  @Bean MyEventService ev(StreamSubscriptionManager s) { return new MyEventService(s)}

}

@Component
class MyEventService extends EventDrivenDomainService {

  @Autowired
  EventClient client

  EventTypeNotFound ev;
  SecondEvent ev2

  MyEventService(StreamSubscriptionManager streamSubscriptionManager) {
    super(streamSubscriptionManager)
    println "Made a service!"
  }

  @EventHandler
  void on(EventTypeNotFound eventTypeNotFound){
    this.ev = eventTypeNotFound;
  }

  @EventHandler
  void on(FirstEvent eventTypeNotFound){
    println "Got MyEvent"
    client.event(ClientEvent.ofType(SecondEvent.simpleName).stream("newstream").payload(new SecondEvent(id:"12345")).build())
  }

  @EventHandler
  void on(SecondEvent eventTypeNotFound){
    println "Got other event"
    ev2 = eventTypeNotFound
  }

  @Override
  protected String[] getStreams() {
    ["mystream", "newstream"]
  }
}


class SecondEvent extends TenantEvent<String> {
  String id
}

class FirstEvent extends TenantEvent<String> {
  String id
}
