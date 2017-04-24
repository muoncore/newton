package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SagaStartCache {

  private Map<Class<? extends NewtonEvent>, List<Class<? extends Saga>>> sagasToStart = new HashMap<>();

  public void add(Class<? extends NewtonEvent> evClass, Class<? extends Saga> sagaClass) {
    log.info("Adding saga to start {} on {}", sagaClass, evClass);
    List<Class<? extends Saga>> classes = sagasToStart.getOrDefault(evClass, new ArrayList<>());
    classes.add(sagaClass);
    sagasToStart.put(evClass, classes);
  }

  public List<Class<? extends Saga>> find(Class<? extends NewtonEvent> evClass) {
    return sagasToStart.getOrDefault(evClass, new ArrayList<>());
  }
}
