package io.muoncore.newton.query;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.streams.BaseStreamSubscriber;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Base class for views that rebuild on startup
 * This means that they will not use a lock of any kind, with each instance running independently
 */
@Slf4j
public abstract class RebuildingDatastoreView extends BaseStreamSubscriber {

  public RebuildingDatastoreView(StreamSubscriptionManager streamSubscriptionManager)  {
    super(streamSubscriptionManager);
  }

  protected Consumer<Consumer<NewtonEvent>> run(String stream) {
    return consumer -> streamSubscriptionManager.localNonTrackingSubscription(stream, consumer);
  }
}
