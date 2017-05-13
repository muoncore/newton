package io.muoncore.newton.todo;

import io.muoncore.newton.support.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskView {

  private DocumentId id;
  private String description;
}
