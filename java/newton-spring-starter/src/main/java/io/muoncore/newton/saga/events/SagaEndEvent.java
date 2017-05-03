package io.muoncore.newton.saga.events;

import io.muoncore.newton.AggregateRootId;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SagaEndEvent implements SagaLifecycleEvent {
    private AggregateRootId id;
}
