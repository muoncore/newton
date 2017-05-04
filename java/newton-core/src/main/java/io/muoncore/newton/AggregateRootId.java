package io.muoncore.newton;

import com.google.gson.annotations.SerializedName;

//todo: try to rather make this an interface (consider gson serlization issues)
public class AggregateRootId {

  @SerializedName("value")
  protected String value;

  public String getValue(){
    return value;
  }

}
