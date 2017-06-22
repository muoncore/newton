package utils

import io.muoncore.protocol.event.client.EventClient
import io.muoncore.protocol.event.client.EventReplayMode
import org.junit.rules.ExternalResource
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class EventSubscriptionResource extends ExternalResource {

  EventClient eventClient
  private String streamName
  private String serviceId

  def events = []

  EventSubscriptionResource(def serviceId, def streamName) {
    this.serviceId = serviceId
    this.streamName = streamName
  }

  def getEventsRaised() {
    return this.events
  }

  @Override
  protected void before() throws Throwable {
    this.eventClient = MuonEventClientHelper.create(this.serviceId)

    this.eventClient.replay(this.serviceId.concat("/").concat(streamName), EventReplayMode.LIVE_ONLY, ["from": 0], new Subscriber() {
      @Override
      void onSubscribe(Subscription s) {
        s.request(Integer.MAX_VALUE)
      }

      @Override
      void onNext(Object o) {
        events << o
      }

      @Override
      void onError(Throwable t) {
        t.printStackTrace()
      }

      @Override
      void onComplete() {
        println "Completed"
      }
    })


  }

  @Override
  protected void after() {
    this.events.clear()
  }
}
