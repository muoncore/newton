package io.muoncore.newton.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SagaInterest {
    private String sagaClassName;
    private String className;
    private Object id;
    private String sagaId;
    private String key;
    private String value;
}
