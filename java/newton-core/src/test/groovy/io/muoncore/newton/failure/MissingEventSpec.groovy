package io.muoncore.newton.failure

import io.muoncore.newton.EventHandler
import io.muoncore.newton.InMemoryTestConfiguration
import io.muoncore.newton.NewtonEvent
import io.muoncore.newton.NewtonEventClient
import io.muoncore.newton.StreamSubscriptionManager
import io.muoncore.newton.domainservice.EventDrivenDomainService
import io.muoncore.newton.eventsource.EventTypeNotFound
import io.muoncore.protocol.event.ClientEvent
import io.muoncore.protocol.event.client.EventClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@ActiveProfiles("test")
@SpringBootTest(classes = [InMemoryTestConfiguration, FailConfig])
class MissingEventSpec extends Specification {

  @Autowired
  MyEventService eventService

  @Autowired
  NewtonEventClient eventClient

  def "when receives a non existent event type, is cast to EventTypeNotFound"() {

    when: "emit an event with an unmappable type"
    eventClient.event(ClientEvent.ofType("NonExistingClass").stream("mystream").build())

    then:
    new PollingConditions().eventually {
      eventService.ev?.payload?.eventType == "NonExistingClass"
    }
  }

  def "event service can emit events"() {
    when:
    println "EMIT WAS " + eventClient.event(ClientEvent.ofType(InitialEventType.simpleName).stream("newstream").payload(new InitialEventType(id:"12345")).build()).status

    then:
    new PollingConditions().eventually {
      eventService.ev2
    }
  }


}

@Configuration
class FailConfig {

  @Bean MyEventService ev(StreamSubscriptionManager s) { return new MyEventService(s)}

}

@Component
class MyEventService extends EventDrivenDomainService {

  @Autowired
  NewtonEventClient client

  EventTypeNotFound ev;
  SubsequentEvent ev2

  MyEventService(StreamSubscriptionManager streamSubscriptionManager) {
    super(streamSubscriptionManager)
  }

  @EventHandler
  void on(EventTypeNotFound eventTypeNotFound){
    this.ev = eventTypeNotFound;
  }

  @EventHandler
  void on(InitialEventType eventTypeNotFound){
    println "Got MyEvent"
    client.event(ClientEvent.ofType(SubsequentEvent.simpleName).stream("newstream").payload(new SubsequentEvent(id:"12345")).build())
  }

  @EventHandler
  void on(SubsequentEvent eventTypeNotFound){
    println "Got other event"
    ev2 = eventTypeNotFound
  }

  @Override
  protected String[] getStreams() {
    ["mystream", "newstream"]
  }
}


class SubsequentEvent implements NewtonEvent<String> {
  String id
}

class InitialEventType implements NewtonEvent<String> {
  String id
}
