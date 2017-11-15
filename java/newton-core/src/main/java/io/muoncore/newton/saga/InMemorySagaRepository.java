package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

@Slf4j
public class InMemorySagaRepository implements SagaRepository {

  private Map<String, Saga> sagaStore = new HashMap<>();
  private Map<Object, List<SagaCreated>> sagaCreatedStore = new HashMap<>();
  private Map<String, Set<SagaInterest>> sagaInterestStore = new HashMap<>();

  @Override
  public <T extends Saga> Optional<T> load(String sagaIdentifier, Class<T> type) {
    Saga saga = sagaStore.get(sagaIdentifier);
    //noinspection unchecked
    return Optional.ofNullable((T) saga);
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
    List<SagaInterest> interests = saga.getNewSagaInterests();

    for (SagaInterest interest : interests) {
      final String className = interest.getClassName();
      Predicate<SagaInterest> hasNotSameSagaId = i -> !i.getSagaId().equals(saga.getId());

      Set<SagaInterest> storedInterests = sagaInterestStore.get(className);
      sagaInterestStore.put(className,
                            storedInterests.stream()
                                           .filter(hasNotSameSagaId)
                                           .collect(Collectors.toSet())
      );
    }

    log.debug("Saga is complete, removed interests where necessary.");
  }

  private void registerEventExpectation(SagaInterest sagaInterest) {
    log.debug("Persisting Saga interest " + sagaInterest);
    Set<SagaInterest> newInterests = new HashSet<>();
    Set<SagaInterest> currentInterests = sagaInterestStore.get(sagaInterest.getClassName());

    if (currentInterests != null) {
      newInterests.addAll(currentInterests);
    }

    newInterests.add(sagaInterest);
    sagaInterestStore.put(sagaInterest.getClassName(), newInterests);
  }

  @Override
  public List<SagaInterest> getSagasInterestedIn(Class<? extends NewtonEvent> eventClass) {
    String eventName = eventClass.getName();
    return new ArrayList<>(sagaInterestStore.getOrDefault(eventName, emptySet()));
  }

  @Override
  public List<SagaCreated> getSagasCreatedByEventId(Object id) {
    return sagaCreatedStore.get(id);
  }

  protected Map<String, Saga> getSagaStore() {
    return this.sagaStore;
  }

  protected Map<Object, List<SagaCreated>> getSagaCreatedStore() {
    return sagaCreatedStore;
  }

  protected Map<String, Set<SagaInterest>> getSagaInterestStore() {
    return sagaInterestStore;
  }
}
