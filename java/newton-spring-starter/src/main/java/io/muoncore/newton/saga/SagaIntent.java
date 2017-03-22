package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.NewtonIdentifier;
import lombok.Getter;

public class SagaIntent<ID extends NewtonIdentifier, T extends Saga<P, ID>, P extends NewtonEvent> {
    @Getter
    private Class<T> type;
    @Getter
    private P payload;

    public SagaIntent(Class<T> type, P payload) {
        this.type = type;
        this.payload = payload;
    }
}
