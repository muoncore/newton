package io.muoncore.newton.domainservice;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.streams.BaseStreamSubscriber;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Base class for domain services.
 *
 * Manages event processing in a globally consistent manner.
 */
@Slf4j
public abstract class EventDrivenDomainService extends BaseStreamSubscriber {

  public EventDrivenDomainService(StreamSubscriptionManager streamSubscriptionManager) throws IOException {
    super(streamSubscriptionManager);
  }

  @Override
  protected Consumer<Consumer<NewtonEvent>> run(String stream) {
    return consumer -> {
      streamSubscriptionManager.globallyUniqueSubscription(getClass().getSimpleName() + "-" + stream, stream, consumer);
    };
  }

  @Override
  protected String[] eventStreams() {
    return new String[0];
  }
}
