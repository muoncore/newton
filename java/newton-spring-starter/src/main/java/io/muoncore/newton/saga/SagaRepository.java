package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;

import java.util.List;
import java.util.Optional;

public interface SagaRepository {
    <T extends Saga> Optional<T> load(String sagaIdentifier, Class<T> type);
    void save(Saga saga);
    void saveNewSaga(Saga saga, NewtonEvent event);

    List<SagaInterest> getSagasInterestedIn(Class<? extends NewtonEvent> eventClass);
    List<SagaCreated> getSagasCreatedByEventId(Object id);
}
