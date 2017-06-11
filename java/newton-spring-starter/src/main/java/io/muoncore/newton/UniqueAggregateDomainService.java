package io.muoncore.newton;

public interface UniqueAggregateDomainService<V> {
  boolean isUnique(Object thisId, V value);

  boolean exists(V value);

  boolean exists(Object thisId, V value);

  void addValue(Object id, V value);

  void removeValue(Object id);

  void updateValue(Object id, V value);
}
