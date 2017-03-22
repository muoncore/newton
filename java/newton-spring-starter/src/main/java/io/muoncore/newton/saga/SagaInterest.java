package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SagaInterest<ID extends NewtonIdentifier> {
    private String sagaClassName;
    private String className;
    private ID id;
    private String key;
    private String value;
}
