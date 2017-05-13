package io.muoncore.newton.todo;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.eventsource.AggregateConfiguration;
import io.muoncore.newton.support.DocumentId;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@AggregateConfiguration(context = "newton-sample")
public class Task extends AggregateRoot<DocumentId> {

  @Getter
  private DocumentId id;
  private String description;

  public Task(){}

  public Task(@NotNull DocumentId id, @NotNull String description){
    raiseEvent(new TaskCreatedEvent(id, description));
  }

  public void changeDescription(String description) {
    raiseEvent(new TaskDescriptionChangedEvent(this.id, description));
  }

  @EventHandler
  public void handle(TaskCreatedEvent event){
    this.id = event.getId();
    this.description = event.getDescription();
  }

  @EventHandler
  public void handle(TaskDescriptionChangedEvent event){
    this.description = event.getDescription();
  }

}
