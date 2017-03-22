package io.muoncore.newton.command;

public interface CommandBus {
	void dispatch(CommandIntent commandIntent);
}
