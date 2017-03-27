package io.muoncore.newton.command;

import io.muoncore.newton.DocumentId;

public interface IdentifiableCommand<T extends DocumentId> extends Command {

	void setId(T id);

}
