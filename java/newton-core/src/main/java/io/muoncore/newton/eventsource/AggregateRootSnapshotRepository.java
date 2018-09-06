package io.muoncore.newton.eventsource;

import io.muoncore.newton.AggregateRoot;

import java.util.Optional;

/**
 * Persists snapshots of aggregate roots.
 */
public interface AggregateRootSnapshotRepository {
  void persist(AggregateRoot aggregate);
  Optional<AggregateRoot> getLatestSnapshot(Object ID);
}
