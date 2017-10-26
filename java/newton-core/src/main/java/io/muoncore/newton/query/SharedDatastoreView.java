package io.muoncore.newton.query;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.streams.BaseStreamSubscriber;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Base class for views that mutate a shared data store.
 * This means that they will use a cluster lock to give process once semantics.
 *
 * This also handles unpacking tenancy information and applying it to the current Thread for
 * use in the mongo filters. As such, it should generally be used within the multi tenant services
 * as the base view.
 */
@Slf4j
public abstract class SharedDatastoreView extends BaseStreamSubscriber {

  public SharedDatastoreView(StreamSubscriptionManager streamSubscriptionManager) {
    super(streamSubscriptionManager);
  }

  @Override
  protected Consumer<Consumer<NewtonEvent>> run(String stream) {
    return consumer -> {
      streamSubscriptionManager.globallyUniqueSubscription(getClass().getSimpleName() + "-" + stream, stream, consumer);
    };
  }
}
