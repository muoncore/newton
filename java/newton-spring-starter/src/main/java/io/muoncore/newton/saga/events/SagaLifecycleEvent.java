package io.muoncore.newton.saga.events;


import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.DocumentId;

public interface SagaLifecycleEvent extends NewtonEvent {
  DocumentId getId();
}
