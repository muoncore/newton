package io.muoncore.newton.saga.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SagaEndEvent implements SagaLifecycleEvent {
  private String id;
}
