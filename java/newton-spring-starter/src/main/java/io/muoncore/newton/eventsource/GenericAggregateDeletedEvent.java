package io.muoncore.newton.eventsource;


import io.muoncore.newton.NewtonEvent;
import lombok.Data;

//TODO: rework MuonLookupTools to support more than 1st level subtype from NewtonEvent.
@Data
public class GenericAggregateDeletedEvent implements AggregateDeletedEvent<Object>, NewtonEvent<Object> {
    private final Object id;
}
