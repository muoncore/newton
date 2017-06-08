package io.muoncore.newton.command;

import io.muoncore.newton.NewtonEvent;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@AllArgsConstructor
public class CommandResult {

  private List<NewtonEvent> events;
  private CommandFailedEvent failure;

  public Optional<List<NewtonEvent>> getSuccess() {
    if (events == null) {
      return Optional.empty();
    }
    return Optional.of(events);
  }

  public Optional<CommandFailedEvent> getFailure() {
    if (failure == null) {
      return Optional.empty();
    }
    return Optional.of(failure);
  }

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
