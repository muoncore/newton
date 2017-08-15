package io.muoncore.newton.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemEventStreamIndexStore implements EventStreamIndexStore {

  private Map<String, EventStreamIndex> index = new HashMap<>();

  @Override
  public Optional<EventStreamIndex> findOneById(String id) {
    return Optional.ofNullable(index.get(id));
  }

  @Override
  public void save(EventStreamIndex eventStreamIndex) {
    index.put(eventStreamIndex.getStream(), eventStreamIndex);
  }
}
