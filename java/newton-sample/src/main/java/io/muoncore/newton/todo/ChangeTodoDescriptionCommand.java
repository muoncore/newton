package io.muoncore.newton.todo;

import io.muoncore.newton.command.IdentifiableCommand;
import io.muoncore.newton.eventsource.EventSourceRepository;
import io.muoncore.newton.support.DocumentId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ChangeTodoDescriptionCommand implements IdentifiableCommand<DocumentId> {

  private DocumentId id;
  private String description;
  private EventSourceRepository<Todo> todoRepository;

  @Autowired
  public ChangeTodoDescriptionCommand(EventSourceRepository<Todo> todoRepository) {
    this.todoRepository = todoRepository;
  }

  @Override
  public void execute() {
    final Todo todo = this.todoRepository.load(this.id);
    todo.changeDescription(this.description);
  }

  public void setId(DocumentId id) {
    this.id = id;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
