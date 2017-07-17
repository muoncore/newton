package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class SagaStartCache {

  private Map<Class<? extends NewtonEvent>, Set<Class<? extends Saga>>> sagasToStart = new HashMap<>();

  public void add(Class<? extends NewtonEvent> evClass, Class<? extends Saga> sagaClass) {
    log.info("Adding saga to start {} on {}", sagaClass, evClass);
    Set<Class<? extends Saga>> classes = sagasToStart.getOrDefault(evClass, new HashSet<>());
    if (classes.contains(sagaClass)) {
      if (log.isTraceEnabled()){
        log.debug("Saga Start Cache already contains type saga type " + sagaClass + " for event " + evClass);
      }
    }
    classes.add(sagaClass);
    sagasToStart.put(evClass, classes);
  }

  public Set<Class<? extends Saga>> find(Class<? extends NewtonEvent> evClass) {
    return sagasToStart.getOrDefault(evClass, new HashSet<>());
  }
}
