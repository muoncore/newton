package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonIdentifier;
import io.muoncore.newton.UUIDIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SagaInterest<ID extends UUIDIdentifier> {
    private String sagaClassName;
    private String className;
    private ID id;
    private ID sagaId;
    private String key;
    private String value;
}
