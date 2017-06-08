package io.muoncore.newton.eventsource;

import io.muoncore.newton.NewtonEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AggregateDeletedEvent implements NewtonEvent<Object> {
  private Object id;
}
