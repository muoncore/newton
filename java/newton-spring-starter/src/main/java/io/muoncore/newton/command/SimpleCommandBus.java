package io.muoncore.newton.command;

import io.muoncore.api.ImmediateReturnFuture;
import io.muoncore.api.MuonFuture;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

public class SimpleCommandBus implements CommandBus {

	private CommandFactory commandFactory;

	@Autowired
	public SimpleCommandBus(CommandFactory commandFactory) {
		this.commandFactory = commandFactory;
	}

	@Override
	public MuonFuture<CommandResult> dispatch(CommandIntent commandIntent) {
		try {
			Command command = commandFactory.create((Class<Command>) Class.forName(commandIntent.getType()),
					commandIntent.getPayload(),
					commandIntent.getId(),
					commandIntent.getAdditionalProperties(), null);
			return new ImmediateReturnFuture<>(new CommandResult(command.executeAndReturnEvents(), null));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(String.format("Command of type '%s' does not exist", commandIntent.getType()));
		} catch (Exception e) {
		  return new ImmediateReturnFuture<>(
		    new CommandResult(Collections.emptyList(), new CommandFailedEvent(commandIntent.getType(), e.getMessage(), e))
      );
    }
  }
}
