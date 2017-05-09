package io.muoncore.newton;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

//todo: try to rather make this an interface (consider gson serlization issues)
public class AggregateRootId {

  @SerializedName("value")
  protected String value;

  public String getValue(){
    return value;
  }

  public void setValue(String value){
    this.value = value;
  }

  public AggregateRootId(String value) {
    this.value = value;
  }

  //todo: review - think this is required gson but this is dangerous to expose as it will cause nullpointers
  public AggregateRootId(){}

  public static AggregateRootId createRandom(){
    return new AggregateRootId(UUID.randomUUID().toString());
  }

  @Override
  public String toString() {
    return getValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AggregateRootId)) return false;

    AggregateRootId that = (AggregateRootId) o;

    return value != null ? value.equals(that.value) : that.value == null;
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }

}
