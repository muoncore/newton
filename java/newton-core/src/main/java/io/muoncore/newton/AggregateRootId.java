package io.muoncore.newton;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

import java.util.UUID;

//todo: try to rather make this an interface (consider gson serlization issues)
@AllArgsConstructor
public class AggregateRootId {

  @SerializedName("value")
  protected String value;

  public String getValue(){
    return value;
  }

  public AggregateRootId() {
    this.value = UUID.randomUUID().toString();
  }

  @Override
  public String toString() {
    return getValue();
  }

  @Override
  public boolean equals(Object o) {
    return value.equals(o.toString());
  }
}
