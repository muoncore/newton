package io.muoncore.newton.saga;

public interface SagaLoader {
    Class<? extends Saga> loadSagaClass(SagaInterest interest) throws ClassNotFoundException;
}
