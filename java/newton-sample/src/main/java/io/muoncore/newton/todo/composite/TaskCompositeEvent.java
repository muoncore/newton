package io.muoncore.newton.todo.composite;

import io.muoncore.newton.support.DocumentId;
import io.muoncore.newton.support.TenantContextHolder;
import io.muoncore.newton.todo.TenantEvent;
import lombok.Data;

@Data
public class TaskCompositeEvent extends TenantEvent<DocumentId> {

  private final DocumentId id;
  private final String description;

  public TaskCompositeEvent(DocumentId id, String description) {
    this.id = id;
    this.description = description;
    this.setTenantId(TenantContextHolder.getTenantId());
  }


}
