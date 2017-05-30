package io.muoncore.newton.command;

import io.muoncore.newton.NewtonEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CommandFailedEvent implements NewtonEvent<String> {
  private final String id = UUID.randomUUID().toString();
  private String commandName;
  private String failureMessage;
  private Object payload;
}
