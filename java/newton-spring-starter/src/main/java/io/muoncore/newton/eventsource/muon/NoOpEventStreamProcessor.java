package io.muoncore.newton.eventsource.muon;

import io.muoncore.newton.NewtonEvent;

import java.util.List;
import java.util.function.Consumer;

public class NoOpEventStreamProcessor implements EventStreamProcessor {
    @Override
    public List<? extends NewtonEvent> processForPersistence(List<NewtonEvent> events) {
        return events;
    }

    @Override
    public void executeWithinEventContext(NewtonEvent event, Consumer<NewtonEvent> exec) {
        exec.accept(event);
    }

    @Override
    public List<? extends NewtonEvent> processForLoad(List<NewtonEvent> events) {
        return events;
    }
}
