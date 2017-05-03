package io.muoncore.newton.saga;

import io.muoncore.newton.AggregateRootId;;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SagaInterest {
    private String sagaClassName;
    private String className;
    private AggregateRootId id;
    private AggregateRootId sagaId;
    private String key;
    private String value;
}
