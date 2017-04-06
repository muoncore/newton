package io.muoncore.newton.saga;

import io.muoncore.newton.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SagaInterest {
    private String sagaClassName;
    private String className;
    private DocumentId id;
    private DocumentId sagaId;
    private String key;
    private String value;
}
