package io.muoncore.newton;

import java.util.UUID;

//todo: try to rather make this an interface (consider gson serlization issues)
public class AggregateRootId {

  private String value = UUID.randomUUID().toString();

  public AggregateRootId(){}

  public AggregateRootId(String value){
    this.value = value;
  }

  public String getValue(){
    return value;
  }

}
