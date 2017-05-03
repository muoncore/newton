package io.muoncore.newton.saga.events;


import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.AggregateRootId;

public interface SagaLifecycleEvent extends NewtonEvent {
  AggregateRootId getId();
}
