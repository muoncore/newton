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
