package io.muoncore.newton.eventsource.muon;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.eventsource.AggregateRootSnapshotRepository;

import java.util.Optional;

public class NoOpAggregateRootSnapshotRepository implements AggregateRootSnapshotRepository {
  @Override
  public void persist(AggregateRoot aggregate) {

  }

  @Override
  public Optional<AggregateRoot> getLatestSnapshot(Object ID) {
    return Optional.empty();
  }
}
