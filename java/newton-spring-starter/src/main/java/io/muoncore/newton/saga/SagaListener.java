package io.muoncore.newton.saga;

interface SagaListener<T extends Saga> {
    void onComplete(T saga);
}
