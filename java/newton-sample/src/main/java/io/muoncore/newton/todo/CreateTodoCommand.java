package io.muoncore.newton.todo;

import io.muoncore.newton.command.Command;
import io.muoncore.newton.eventsource.muon.MuonEventSourceRepository;
import io.muoncore.newton.support.BusinessException;
import io.muoncore.newton.support.DocumentId;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class CreateTodoCommand implements Command{

  private MuonEventSourceRepository<Todo> repository;

  private UniqueTodoDescriptionDomainService uniqueTodoDescriptionDomainService;

  @Setter
  private String description;
  @Setter
  private DocumentId id;


  @Autowired
  public CreateTodoCommand(MuonEventSourceRepository<Todo> repository, UniqueTodoDescriptionDomainService uniqueTodoDescriptionDomainService) {
    this.repository = repository;
    this.uniqueTodoDescriptionDomainService = uniqueTodoDescriptionDomainService;
  }

  @Override
  public void execute() {
    if (uniqueTodoDescriptionDomainService.exists(description)){
      throw new BusinessException("Todo description not unique!");
    }

    Todo todo = new Todo(this.id, this.description);
    repository.save(todo);
  }
}
