package io.muoncore.newton.command;

import io.muoncore.api.MuonFuture;

import java.util.concurrent.CompletableFuture;

public interface CommandBus {
	MuonFuture<CommandResult> dispatch(CommandIntent commandIntent);
  CompletableFuture<CommandResult> dispatchAsync(CommandIntent commandIntent);
}
