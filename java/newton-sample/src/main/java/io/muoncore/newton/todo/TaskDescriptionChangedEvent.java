package io.muoncore.newton.todo;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.support.DocumentId;
import lombok.Data;

@Data
public class TaskDescriptionChangedEvent implements NewtonEvent {

  private final DocumentId id;
  private final String description;

  public TaskDescriptionChangedEvent(DocumentId id, String description) {
    this.id = id;
    this.description = description;
  }
}
