package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.NewtonIdentifier;
import io.muoncore.newton.command.CommandIntent;

import java.util.List;

public interface Saga<T extends NewtonEvent, ID extends NewtonIdentifier> {
    ID getId();
    boolean isComplete();
    void start(T event);
    void handle(NewtonEvent event);
    List<CommandIntent> getNewOperations();
    List<SagaInterest> getNewSagaInterests();
}
