package io.muoncore.newton.todo;

import io.muoncore.newton.command.Command;
import io.muoncore.newton.eventsource.EventSourceRepository;
import io.muoncore.newton.support.DocumentId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ChangeTaskDescriptionCommand implements Command {

  private DocumentId id;
  private String description;
  private EventSourceRepository<Task> todoRepository;

  @Autowired
  public ChangeTaskDescriptionCommand(EventSourceRepository<Task> todoRepository) {
    this.todoRepository = todoRepository;
  }

  @Override
  public void execute() {
    final Task task = this.todoRepository.load(this.id);
    task.changeDescription(this.description);
    todoRepository.save(task);
  }

  public void setId(DocumentId id) {
    this.id = id;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
