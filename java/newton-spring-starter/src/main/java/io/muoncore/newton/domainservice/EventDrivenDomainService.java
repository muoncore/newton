package io.muoncore.newton.domainservice;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import io.muoncore.newton.streams.BaseStreamSubscriber;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Base class for domain services that respond to events on system streams.
 *
 * Will operate in a globally unique way, events will be processed on a single instance across the cluster.
 */
@Slf4j
public abstract class EventDrivenDomainService extends BaseStreamSubscriber {

  public EventDrivenDomainService(StreamSubscriptionManager streamSubscriptionManager, EventStreamProcessor eventStreamProcessor) throws IOException {
    super(streamSubscriptionManager, eventStreamProcessor);
  }

  @Override
  protected Consumer<Consumer<NewtonEvent>> run(String stream) {
    return consumer -> {
      streamSubscriptionManager.globallyUniqueSubscription(getClass().getSimpleName() + "-" + stream, stream, consumer);
    };
  }
}
