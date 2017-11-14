package io.muoncore.newton.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * Describe a subsequent step in a saga workflow.
 * These are created by sagas during their running via {@link StatefulSaga#notifyOn}
 *
 * When a saga completes processing by invoking {@link StatefulSaga#end()} then all pending interests will be cleared.
 */
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
