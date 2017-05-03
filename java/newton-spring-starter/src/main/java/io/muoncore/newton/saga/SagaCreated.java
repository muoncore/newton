package io.muoncore.newton.saga;

import io.muoncore.newton.AggregateRootId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SagaCreated {
    private String sagaClassName;
    private AggregateRootId eventId;
    private AggregateRootId sagaId;
}
