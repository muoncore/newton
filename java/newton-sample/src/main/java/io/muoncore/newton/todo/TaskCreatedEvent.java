package io.muoncore.newton.todo;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.support.DocumentId;
import lombok.Getter;

@Getter
public class TaskCreatedEvent implements NewtonEvent<DocumentId> {

  private final DocumentId id;
  private final String description;

  public TaskCreatedEvent(DocumentId id, String description) {
    this.id = id;
    this.description = description;
  }
}
