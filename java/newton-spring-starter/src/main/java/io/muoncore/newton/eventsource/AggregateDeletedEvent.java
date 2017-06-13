package io.muoncore.newton.eventsource;

import io.muoncore.newton.NewtonEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface AggregateDeletedEvent<T> extends NewtonEvent<T> {
}
