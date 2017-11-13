package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.*;

@Slf4j
public class InMemorySagaRepository implements SagaRepository {

  private Map<String, Saga> sagaStore = new HashMap<>();
  private Map<Object, List<SagaCreated>> sagaCreatedStore = new HashMap<>();
  private Map<String, List<SagaInterest>> sagaInterestStore = new HashMap<>();

  @Override
  public <T extends Saga> Optional<T> load(String sagaIdentifier, Class<T> type) {
    Saga saga = sagaStore.get(sagaIdentifier);
    Optional<T> o = Optional.ofNullable((T) saga); // Dodgy, but maybe good enough for the default impl.
    return o;
  }

  @Override
  public void save(Saga saga) {
    if (saga.isComplete()) {
      clearInterests(saga);
    }
    saga.getNewSagaInterests().forEach(this::registerEventExpectation);
    sagaStore.put(saga.getId(), saga);
  }

  @Override
  public void saveNewSaga(Saga saga, NewtonEvent ev) {
    Assert.notNull(saga, "Saga is Required");
    Assert.notNull(saga.getId(), "Saga ID is Required");
    save(saga);
    updateSagaCreated(saga, ev);
  }

  private void updateSagaCreated(Saga saga, NewtonEvent event) {
    Object eventId = event.getId();
    SagaCreated sc = new SagaCreated(saga.getClass().getName(), eventId, saga.getId());
    List<SagaCreated> sagasCreated = new ArrayList<>();
    List<SagaCreated> sagasAlreadyCreated = sagaCreatedStore.get(eventId);

    if (sagasAlreadyCreated != null) {
      sagasCreated.addAll(sagasAlreadyCreated);
    }

    sagasCreated.add(sc);
    sagaCreatedStore.put(eventId, sagasCreated);
  }

  private void clearInterests(Saga saga) {
    // TODO RP: I've just realised this is wrong... 13/11/17 19:31
    List<SagaInterest> interests = sagaInterestStore.remove(saga.getId());
    log.debug("Saga is complete, removed {} interests"); //, interests.size());
  }

  private void registerEventExpectation(SagaInterest sagaInterest) {
    log.debug("Persisting Saga interest " + sagaInterest);
    List<SagaInterest> newInterests = new ArrayList<>();
    List<SagaInterest> currentInterests = sagaInterestStore.get(sagaInterest.getClassName());

    if (currentInterests != null) {
      newInterests.addAll(currentInterests);
    }

    newInterests.add(sagaInterest);
    sagaInterestStore.put(sagaInterest.getClassName(), newInterests);
  }

  @Override
  public List<SagaInterest> getSagasInterestedIn(Class<? extends NewtonEvent> eventClass) {
    String eventName = eventClass.getName();
    return sagaInterestStore.get(eventName);
  }

  @Override
  public List<SagaCreated> getSagasCreatedByEventId(Object id) {
    return sagaCreatedStore.get(id);
  }
}
