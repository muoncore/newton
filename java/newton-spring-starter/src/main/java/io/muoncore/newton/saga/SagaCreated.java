package io.muoncore.newton.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SagaCreated {
    private String sagaClassName;
    private Object eventId;
    private String sagaId;
}
