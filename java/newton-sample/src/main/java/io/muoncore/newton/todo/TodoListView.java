package io.muoncore.newton.todo;

import io.muoncore.newton.AggregateRootId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodoListView {

  private AggregateRootId id;
  private String description;
}
