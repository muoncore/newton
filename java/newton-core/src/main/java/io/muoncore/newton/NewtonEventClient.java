package io.muoncore.newton;


import io.muoncore.api.MuonFuture;
import io.muoncore.exception.MuonException;
import io.muoncore.protocol.Auth;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.EventBuilder;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventReplayControl;
import io.muoncore.protocol.event.client.EventReplayMode;
import io.muoncore.protocol.event.client.EventResult;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class NewtonEventClient {

  private EventClient client;
  private Auth auth;

  public NewtonEventClient(EventClient client, String authToken) {
    this.client = client;
    this.auth = new Auth("aether", authToken);
  }

  /**
   * Publish the given events to the provided aggregate event stream
   *
   * The ID should be for the aggregate root
   *
   * The events will be converted into Muon `Event` types
   * * stream will be /aggregate/[id]
   * * type will be the event type class Simple Name - eg co.myapp.UserCreatedEvent to UserCreatedEvent
   *
   * @param id
   * @param events
   */
  public void publishDomainEvents(String id, Class type, List events, NewtonEventWithMeta cause) {
    events.forEach(domainEvent -> {

      EventBuilder payload = ClientEvent
        .ofType(domainEvent.getClass().getSimpleName())
        .id(id)
        .stream(createAggregateStreamName(id, type))
        .payload(domainEvent);

      if (cause != null) {
        payload.causedBy(String.valueOf(cause.getMeta().getOrderId()), "CAUSED");
      }

      ClientEvent ev = payload.build();

      EventResult result = client.event(ev, auth);

      if (result.getStatus() == EventResult.EventResultStatus.FAILED) {
        throw new MuonException("Failed to persist domain event " + domainEvent + ":" + result.getCause());
      }
    });
  }

  public CompletableFuture<List<Event>> loadAggregateRoot(String id, Class type) throws InterruptedException {

    List<Event> events = new ArrayList<>();
    CompletableFuture<List<Event>> ret = new CompletableFuture<>();

    String stream = createAggregateStreamName(id, type);

    client.replay(stream, auth, EventReplayMode.REPLAY_ONLY, new Subscriber<Event>() {
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

  public String createAggregateStreamName(String id, Class type) {
    return "/aggregate/" + type.getSimpleName() + "/" + id;
  }

  public EventResult event(ClientEvent event) {
    return this.client.event(event, auth);
  }

  public MuonFuture<EventResult> eventAsync(ClientEvent event) {
    return this.client.eventAsync(event, auth);
  }

  public <X> MuonFuture<EventReplayControl> replay(String streamName, EventReplayMode mode, Subscriber<Event> subscriber) {
    return this.client.replay(streamName, auth, mode, subscriber);
  }

  public <X> MuonFuture<EventReplayControl> replay(String streamName, EventReplayMode mode, Map<String, Object> args, Subscriber<Event> subscriber) {
    return this.client.replay(streamName, auth, mode, args, subscriber);
  }
}
