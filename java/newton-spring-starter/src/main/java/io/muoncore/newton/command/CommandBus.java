package io.muoncore.newton.command;

import io.muoncore.api.MuonFuture;

public interface CommandBus {
	MuonFuture<CommandResult> dispatch(CommandIntent commandIntent);
}
