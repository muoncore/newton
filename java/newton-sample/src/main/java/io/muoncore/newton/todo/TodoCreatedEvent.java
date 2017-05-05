package io.muoncore.newton.todo;

import io.muoncore.newton.AggregateRootId;
import io.muoncore.newton.NewtonEvent;
import lombok.Getter;

@Getter
public class TodoCreatedEvent implements NewtonEvent {

  private final AggregateRootId id;
  private final String description;

  public TodoCreatedEvent(AggregateRootId id, String description) {
    this.id = id;
    this.description = description;
  }
}
