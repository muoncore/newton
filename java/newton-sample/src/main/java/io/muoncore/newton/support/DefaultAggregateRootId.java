package io.muoncore.newton.support;

import io.muoncore.newton.AggregateRootId;

import java.util.UUID;

public class DefaultAggregateRootId extends AggregateRootId {

  public DefaultAggregateRootId(){
    this.value = UUID.randomUUID().toString();
  }
}
