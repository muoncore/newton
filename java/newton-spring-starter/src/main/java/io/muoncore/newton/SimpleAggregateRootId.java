package io.muoncore.newton;

import java.util.UUID;

//todo: get rid of this if we're unable to make AggregateRootId an interface
public class SimpleAggregateRootId extends AggregateRootId{

  public SimpleAggregateRootId(){
    super(UUID.randomUUID().toString());
  }

  public SimpleAggregateRootId(String value){
    super(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
