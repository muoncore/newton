package io.muoncore.newton.query;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import io.muoncore.newton.streams.BaseStreamSubscriber;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class BaseView extends BaseStreamSubscriber {

  public BaseView(StreamSubscriptionManager streamSubscriptionManager, EventStreamProcessor eventStreamProcessor) throws IOException {
    super(streamSubscriptionManager, eventStreamProcessor);
  }

  @Override
  protected List<String> streams() {
    return Arrays.asList(getClass().getAnnotation(NewtonView.class).streams());
  }

  @Override
  protected List<Class<? extends AggregateRoot>> aggregateRoots() {
    return Arrays.asList(getClass().getAnnotation(NewtonView.class).aggregateRoot());
  }
}
