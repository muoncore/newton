package io.muoncore.newton.todo;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.support.DocumentId;
import lombok.Data;

@Data
public class TodoDescriptionChangedEvent implements NewtonEvent {

  private final DocumentId id;
  private final String description;

  public TodoDescriptionChangedEvent(DocumentId id, String description) {
    this.id = id;
    this.description = description;
  }
}
