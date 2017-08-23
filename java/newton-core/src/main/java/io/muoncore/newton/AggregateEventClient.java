package io.muoncore.newton;


import io.muoncore.exception.MuonException;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventReplayMode;
import io.muoncore.protocol.event.client.EventResult;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class AggregateEventClient {

  private EventClient client;

  public AggregateEventClient(EventClient client) {
    this.client = client;
  }

  /**
   * Publish the given events to the provided aggregate event stream
   *
   * The ID should be for the aggregate root
   *
   * The events will be converted into Muon `Event` types
   * * stream will be /aggregate/[id]
   * * type will be the event type class Simple Name - eg co.myapp.UserCreatedEvent -> UserCreatedEvent
   *
   * @param id
   * @param events
   */
  public void publishDomainEvents(String id, Class type, List events) {
    events.forEach(domainEvent -> {
      ClientEvent persistEvent = ClientEvent
        .ofType(domainEvent.getClass().getSimpleName())
        .id(id)
        .payload(domainEvent)
        .stream("/aggregate/" + type.getSimpleName() + "/" + id)
        .build();

      EventResult result = client.event(persistEvent);

      if (result.getStatus() == EventResult.EventResultStatus.FAILED) {
        throw new MuonException("Failed to persist domain event " + domainEvent + ":" + result.getCause());
      }
    });
  }

  public CompletableFuture<List<Event>> loadAggregateRoot(String id, Class type) throws InterruptedException {

    List<Event> events = new ArrayList<>();
    CompletableFuture<List<Event>> ret = new CompletableFuture<>();

    String stream = "/aggregate/" + type.getSimpleName() + "/" + id;

    client.replay(stream, EventReplayMode.REPLAY_ONLY, new Subscriber<Event>() {
      @Override
      public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(Event o) {
        events.add(o);
      }

      @Override
      public void onError(Throwable t) {
        log.warn("Failed to load event stream due to a communication failure: {}", t.getMessage());
        ret.completeExceptionally(new EventStoreException(id, stream, t.getMessage()));
      }

      @Override
      public void onComplete() {
        ret.complete(events);
      }
    });

    return ret;
  }
}
