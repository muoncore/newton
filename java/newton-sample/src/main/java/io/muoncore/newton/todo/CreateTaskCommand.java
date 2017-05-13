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
public class CreateTaskCommand implements Command{

  private MuonEventSourceRepository<Task> repository;

  private UniqueTaskDescriptionDomainService uniqueTaskDescriptionDomainService;

  @Setter
  private String description;
  @Setter
  private DocumentId id;


  @Autowired
  public CreateTaskCommand(MuonEventSourceRepository<Task> repository, UniqueTaskDescriptionDomainService uniqueTaskDescriptionDomainService) {
    this.repository = repository;
    this.uniqueTaskDescriptionDomainService = uniqueTaskDescriptionDomainService;
  }

  @Override
  public void execute() {
    if (uniqueTaskDescriptionDomainService.exists(description)){
      throw new BusinessException("Todo description not unique!");
    }

    Task task = new Task(this.id, this.description);
    repository.save(task);
  }
}
