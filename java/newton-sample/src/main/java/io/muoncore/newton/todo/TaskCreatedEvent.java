package io.muoncore.newton.todo;

import io.muoncore.newton.support.DocumentId;
import io.muoncore.newton.support.TenantContextHolder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TaskCreatedEvent extends TenantEvent<DocumentId> {


  private final DocumentId id;
  private final String description;

  public TaskCreatedEvent(DocumentId id, String description) {
    this.id = id;
    this.description = description;
    this.setTenantId(TenantContextHolder.getTenantId());
  }
}
