package io.muoncore.newton;

import com.google.gson.annotations.SerializedName;

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

  public AggregateRootId(){}

  @Override
  public String toString() {
    return getValue();
  }

  @Override
  public boolean equals(Object o) {
    return value.equals(o.toString());
  }
}
