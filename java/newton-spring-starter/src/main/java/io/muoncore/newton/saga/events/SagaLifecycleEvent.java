package io.muoncore.newton.saga.events;


import io.muoncore.newton.NewtonEvent;
import lombok.Getter;

public interface SagaLifecycleEvent extends NewtonEvent {
  String getId();
}
