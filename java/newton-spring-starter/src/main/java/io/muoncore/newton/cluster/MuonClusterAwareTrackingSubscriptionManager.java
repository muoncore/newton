package io.muoncore.newton.cluster;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.eventsource.EventTypeNotFound;
import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import io.muoncore.newton.query.EventStreamIndex;
import io.muoncore.newton.query.EventStreamIndexStore;
import io.muoncore.newton.utils.muon.MuonLookupUtils;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventReplayMode;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
public class MuonClusterAwareTrackingSubscriptionManager implements StreamSubscriptionManager {

  private final int RECONNECTION_BACKOFF = 5000;
  private EventClient eventClient;
  private EventStreamIndexStore eventStreamIndexStore;
  private LockService lockService;
  private EventStreamProcessor eventStreamProcessor;
  //avoid potential deadlock by doing all work on a different thread, not the event dispatch thread.
  private final Executor worker = Executors.newSingleThreadExecutor();
  private final Executor pool = Executors.newCachedThreadPool();

  public MuonClusterAwareTrackingSubscriptionManager(EventClient eventClient, EventStreamIndexStore eventStreamIndexStore, LockService lockService, EventStreamProcessor eventStreamProcessor) {
    this.eventClient = eventClient;
    this.eventStreamIndexStore = eventStreamIndexStore;
    this.lockService = lockService;
    this.eventStreamProcessor = eventStreamProcessor;
  }

  @Override
  public void localNonTrackingSubscription(String streamName, Consumer<NewtonEvent> onData) {
    repeatUntilCleanlyRuns(streamName, () -> {
      subscription(streamName, onData);
    });
  }

  private void repeatUntilCleanlyRuns(String name, Runnable exec) {
    pool.execute(() -> {
      while(true) {
        try {
          exec.run();
          break;
        } catch (Exception e) {
          log.warn("Error on subscription {}, backing of and reconnecting: {}", name, e.getMessage());
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e1) {}
        }
      }
    });
  }

  private void subscription(String streamName, Consumer<NewtonEvent> onData) {
    log.debug("Subscribing to event stream '{}' for full local replay", streamName);

    eventClient.replay(
      streamName,
      EventReplayMode.REPLAY_THEN_LIVE,
      new EventSubscriber(event -> {
        log.debug("NewtonEvent received " + event);
        final NewtonEvent newtonEvent = event.getPayload(MuonLookupUtils.getDomainClass(event));
        worker.execute(() -> {
          eventStreamProcessor.executeWithinEventContext(newtonEvent, onData);
        });
      }, throwable -> {
        log.warn("NewtonEvent subscription has ended, will attempt to reconnect in {}ms", RECONNECTION_BACKOFF);
        try {
          Thread.sleep(RECONNECTION_BACKOFF);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        localNonTrackingSubscription(streamName, onData);
      }));
  }

  @Override
  public void globallyUniqueSubscription(String subscriptionName, String stream, Consumer<NewtonEvent> onData) {
    lockService.executeAndRepeatWithLock(subscriptionName, control -> {
      localTrackingSubscription(subscriptionName, stream, onData, error -> {
        control.releaseLock();
      });
    });
  }

  @Override
  public void localTrackingSubscription(String subscriptionName, String streamName, Consumer<NewtonEvent> onData) {
    repeatUntilCleanlyRuns(subscriptionName, () -> {
      localTrackingSubscription(subscriptionName, streamName, onData, throwable -> {
        log.warn("NewtonEvent subscription has ended, will attempt to reconnect in {}ms", RECONNECTION_BACKOFF);
        try {
          Thread.sleep(RECONNECTION_BACKOFF);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        localTrackingSubscription(subscriptionName, streamName, onData);
      });
    });
  }

  private void localTrackingSubscription(String subscriptionName, String streamName, Consumer<NewtonEvent> onData, Consumer<Throwable> onError) {
    EventStreamIndex eventStreamIndex = getEventStreamIndex(subscriptionName, streamName);

    Long lastSeen = eventStreamIndex.getLastSeen() + 1;

    log.info("Subscribing from index {} to event stream {} '{}'", lastSeen, subscriptionName, streamName);

    Map args = new HashMap();
    args.put("from", lastSeen);
    args.put("sub-name", subscriptionName);

    eventClient.replay(
      streamName,
      EventReplayMode.REPLAY_THEN_LIVE,
      args,
      new EventSubscriber(event -> {
        log.trace("Store is {}, event is {}, time is {}", eventStreamIndexStore, event, event.getOrderId());
        Class<? extends NewtonEvent> eventType = MuonLookupUtils.getDomainClass(event);
        if (log.isTraceEnabled()) {
          log.trace("Store is {}, event is {}, time is {}", eventStreamIndexStore, event, event.getOrderId());
        }

        eventStreamIndexStore.save(new EventStreamIndex(subscriptionName, event.getOrderId()==null?0l:event.getOrderId()));
        NewtonEvent newtonEvent;
        if (eventType == null) {
          newtonEvent = new EventTypeNotFound(event.getOrderId(), event);
        } else {
          newtonEvent = event.getPayload(eventType);
        }
        worker.execute(() -> {
          eventStreamProcessor.executeWithinEventContext(newtonEvent, onData);
        });
      }, onError));
  }

  private EventStreamIndex getEventStreamIndex(String subscriptionName, String streamName) {
    return eventStreamIndexStore.findOneById(subscriptionName).orElse(new EventStreamIndex(streamName, 0L));
  }


  static class EventSubscriber implements Subscriber<io.muoncore.protocol.event.Event> {

    private Consumer<io.muoncore.protocol.event.Event> onData;
    private Consumer<Throwable> onError;

    public EventSubscriber(Consumer<io.muoncore.protocol.event.Event> onData, Consumer<Throwable> onError) {
      this.onData = onData;
      this.onError = onError;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
      subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(io.muoncore.protocol.event.Event event) {
      onData.accept(event);
    }

    @Override
    public void onError(Throwable throwable) {
      log.error("Error in subscription ", throwable);
      onError.accept(throwable);
    }

    @Override
    public void onComplete() {
      log.error("Subscription has completed cleanly, this is unexpected with REPLAY_THEN_LIVE");
      onError.accept(new IllegalStateException("The event store has terminated a stream subscription cleanly. This is not expected with REPLAY_THEN_LIVE"));
    }
  }
}
