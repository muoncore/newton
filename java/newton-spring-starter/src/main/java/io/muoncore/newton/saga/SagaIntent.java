package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import lombok.Getter;

public class SagaIntent<T extends Saga> {
    @Getter
    private Class<T> type;
    @Getter
    private NewtonEvent payload;

    public SagaIntent(Class<T> type, NewtonEvent payload) {
        this.type = type;
        this.payload = payload;
    }
}
