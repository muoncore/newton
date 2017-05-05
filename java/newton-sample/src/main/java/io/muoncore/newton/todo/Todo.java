package io.muoncore.newton.todo;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.AggregateRootId;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.eventsource.AggregateConfiguration;

import javax.validation.constraints.NotNull;

@AggregateConfiguration(context = "newton-sample")
public class Todo extends AggregateRoot<AggregateRootId> {

  private String description;

  public Todo(@NotNull AggregateRootId id, @NotNull String description){
    raiseEvent(new TodoCreatedEvent(id, description));
  }

  @EventHandler
  public void handle(TodoCreatedEvent event){
    this.setId(event.getId());
    this.description = event.getDescription();
  }

}
