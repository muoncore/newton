package io.muoncore.newton;

import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;


public final class DocumentId<D> {
  private ObjectId value;

  public DocumentId() {
    value = new ObjectId();
  }

  private DocumentId(String value) {
    if(!StringUtils.isEmpty(value)) {
      this.value = new ObjectId(value);
    }
  }

  private DocumentId(ObjectId value) {
    this.value = value;
  }

  public static <D> DocumentId<D> valueOf(String value) {
    return new DocumentId(value);
  }

  public static <D> DocumentId<D> valueOf(ObjectId value) {
    return new DocumentId(value);
  }

  public ObjectId getValue() {
    return this.value;
  }

  public String toString() {
    return this.value != null?this.value.toHexString():null;
  }

  public boolean equals(Object o) {
    if(this == o) {
      return true;
    } else if(o != null && this.getClass() == o.getClass()) {
      DocumentId that = (DocumentId)o;
      return this.value != null?this.value.equals(that.value):that.value == null;
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.value != null?this.value.hashCode():0;
  }

  public int compareTo(DocumentId o) {
    return this.getValue() == null && o.getValue() != null?-1:(this.getValue() != null && o.getValue() == null?1:this.getValue().compareTo(o.getValue()));
  }
}

