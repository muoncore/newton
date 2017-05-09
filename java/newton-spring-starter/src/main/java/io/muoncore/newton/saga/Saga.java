package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.command.CommandIntent;

import java.util.List;

public interface Saga {
    String getId();
    boolean isComplete();
    void handle(NewtonEvent event);
    void startWith(NewtonEvent event);
    List<CommandIntent> getNewOperations();
    List<SagaInterest> getNewSagaInterests();
}
