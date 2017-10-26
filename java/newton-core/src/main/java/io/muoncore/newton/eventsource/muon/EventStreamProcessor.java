package io.muoncore.newton.eventsource.muon;


import io.muoncore.newton.NewtonEvent;

import java.util.List;
import java.util.function.Consumer;

/**
 * Used to transform events as they go to/ from persistence and network streaming.
 * Used to implements fixes for serialisation and enforcing/ generating multi tenancy
 */
public interface EventStreamProcessor {
    /**
     * Process events before they are passed through for persistence into Photon
     */
    List<? extends NewtonEvent> processForPersistence(List<NewtonEvent> events);

    /**
     * Process events before they are passed through for persistence into Photon
     */
    List<? extends NewtonEvent> processForLoad(List<? extends NewtonEvent> events);

    /**
     * Setup any necessary environment for a component to process this event.
     * Used to introduce multi tenancy information into a ThreadLocal when it is present
     */
    void executeWithinEventContext(NewtonEvent event, Consumer<NewtonEvent> exec);


}
