package io.muoncore.newton.todo;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.support.DocumentId;
import lombok.Getter;

@Getter
public class TodoCreatedEvent implements NewtonEvent<DocumentId> {

  private final DocumentId id;
  private final String description;

  public TodoCreatedEvent(DocumentId id, String description) {
    this.id = id;
    this.description = description;
  }
}
