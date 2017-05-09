package io.muoncore.newton.todo;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.eventsource.AggregateConfiguration;
import io.muoncore.newton.support.DocumentId;

import javax.validation.constraints.NotNull;

@AggregateConfiguration(context = "newton-sample")
public class Todo extends AggregateRoot<DocumentId> {

  private String description;

  public Todo(){}

  public Todo(@NotNull DocumentId id, @NotNull String description){
    raiseEvent(new TodoCreatedEvent(id, description));
  }

  public void changeDescription(String description) {
    raiseEvent(new TodoDescriptionChangedEvent(this.id, description));
  }

  @EventHandler
  public void handle(TodoCreatedEvent event){
    this.setId(event.getId());
    this.description = event.getDescription();
  }

  @EventHandler
  public void handle(TodoDescriptionChangedEvent event){
    this.description = event.getDescription();
  }

}
