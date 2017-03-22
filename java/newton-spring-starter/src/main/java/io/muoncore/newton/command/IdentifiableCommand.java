package io.muoncore.newton.command;

import io.muoncore.newton.NewtonIdentifier;

public interface IdentifiableCommand<T extends NewtonIdentifier> extends Command {

	void setId(T id);

}
