package io.muoncore.newton.saga;

import io.muoncore.newton.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SagaCreated {
    private String sagaClassName;
    private DocumentId eventId;
    private DocumentId sagaId;
}
