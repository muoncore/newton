package io.muoncore.newton.command;

import io.muoncore.newton.NewtonEvent;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
public class CommandResult {

  private List<NewtonEvent> events;
  private CommandFailedEvent failure;

  public CommandResult onSuccess(Consumer<List<NewtonEvent>> success) {
    if (failure == null) {
      success.accept(events);
    }
    return this;
  }

  public CommandResult onError(Consumer<CommandFailedEvent> failure) {
    if (failure != null) {
      failure.accept(this.failure);
    }
    return this;
  }
}
