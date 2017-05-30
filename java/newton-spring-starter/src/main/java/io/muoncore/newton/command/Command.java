package io.muoncore.newton.command;

import io.muoncore.newton.NewtonEvent;

import java.util.Collections;
import java.util.List;

public interface Command {
	void execute();
	default List<NewtonEvent> executeAndReturnEvents() {
	  execute();
	  return Collections.emptyList();
  }
}
