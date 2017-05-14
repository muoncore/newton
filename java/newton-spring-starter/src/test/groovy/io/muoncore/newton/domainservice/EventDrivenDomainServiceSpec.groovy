package io.muoncore.newton.domainservice

import io.muoncore.newton.AggregateRoot
import io.muoncore.newton.StreamSubscriptionManager
import io.muoncore.newton.eventsource.AggregateConfiguration
import spock.lang.Specification

class EventDrivenDomainServiceSpec extends Specification {

  def "subscribes to both streams and aggregate roots"() {
    def domainservice = new EventDrivenDomainService(Mock(StreamSubscriptionManager)) {
      @Override
      protected List aggregateRoots() {
        [TestingAggregate]
      }

      @Override
      protected String[] eventStreams() {
        return ["/myexternalstream2", "/myexternalstream1"]
      }
    }

    when:
    def streams = domainservice.getStreams() as Set

    then:
    streams == [
      "/myexternalstream1",
      "/myexternalstream2",
      "something/TestingAggregate"
    ] as Set
  }
}


@AggregateConfiguration(context = "something")
class TestingAggregate extends AggregateRoot {
  String id
}
