package io.muoncore.newton.saga.events;


import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.NewtonIdentifier;

public interface SagaLifecycleEvent extends NewtonEvent {
  NewtonIdentifier getId();
}
