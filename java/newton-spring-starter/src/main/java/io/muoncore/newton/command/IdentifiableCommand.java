package io.muoncore.newton.command;

import io.muoncore.newton.AggregateRootId;

public interface IdentifiableCommand<T extends AggregateRootId> extends Command {

	void setId(T id);

}
