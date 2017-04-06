package io.muoncore.newton.command;

import org.springframework.beans.factory.annotation.Autowired;

public class SimpleCommandBus implements CommandBus {

	private CommandFactory commandFactory;

	@Autowired
	public SimpleCommandBus(CommandFactory commandFactory) {
		this.commandFactory = commandFactory;
	}

	@Override
	public void dispatch(CommandIntent commandIntent) {
		try {
			Command command = commandFactory.create((Class<Command>) Class.forName(commandIntent.getType()),
					commandIntent.getPayload(),
					commandIntent.getId(),
					commandIntent.getAdditionalProperties(), null);
			command.execute();
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(String.format("Command of type '%s' does not exist", commandIntent.getType()));
		}
	}
}
