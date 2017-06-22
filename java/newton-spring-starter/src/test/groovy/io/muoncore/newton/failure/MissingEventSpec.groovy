package io.muoncore.newton.failure

import io.muoncore.newton.EnableNewton
import io.muoncore.newton.EventHandler
import io.muoncore.newton.MuonTestConfiguration
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
@SpringBootTest(classes = [MuonTestConfiguration, FailConfig])
class MissingEventSpec extends Specification {

  @Autowired
  MyEventService eventService

  @Autowired
  EventClient eventClient

  def "when receives a non existent event type, is cast to EventTypeNotFound"() {

    when: "emit an event with an unmappable type"
    eventClient.event(ClientEvent.ofType("NonExistingClass").stream("mystream").build())

    then:
    new PollingConditions().eventually {
      eventService.ev?.payload?.eventType == "NonExistingClass"
    }
  }
}

@Configuration
class FailConfig {

  @Bean MyEventService ev(StreamSubscriptionManager s) { return new MyEventService(s)}

}

@Component
class MyEventService extends EventDrivenDomainService {

  EventTypeNotFound ev;

  MyEventService(StreamSubscriptionManager streamSubscriptionManager) {
    super(streamSubscriptionManager)
  }

  @EventHandler
  public void on(EventTypeNotFound eventTypeNotFound){
    this.ev = eventTypeNotFound;
  }

  @Override
  protected String[] getStreams() {
    ["mystream"]
  }
}
