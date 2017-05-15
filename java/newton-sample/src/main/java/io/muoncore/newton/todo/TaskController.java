package io.muoncore.newton.todo;

import io.muoncore.newton.command.CommandBus;
import io.muoncore.newton.command.CommandIntent;
import io.muoncore.newton.support.DocumentId;
import io.muoncore.newton.support.TenantContextHolder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private CommandBus commandBus;
  private TaskViewStore viewStore;

  @Autowired
  public TaskController(CommandBus commandBus, TaskViewStore viewStore) {
    this.commandBus = commandBus;
    this.viewStore = viewStore;
  }

  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> create(@RequestBody @Valid CreateRequest createRequest){
    //todo: remove once a better way is found to set tenantId
    TenantContextHolder.setTenantId("test-tenant");
    final DocumentId id = new DocumentId();
    this.commandBus.dispatch(
      CommandIntent.builder(CreateTaskCommand.class.getName())
        .request(createRequest)
        .id(id)
        .build()
    );
    final URI location = UriComponentsBuilder.fromPath("/api/todos/".concat(id.getValue())).build().toUri();
    return ResponseEntity.created(location).build();
  }

  @RequestMapping(value = "/{id}",method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void changeDescription(@PathVariable("id") DocumentId id, @RequestBody @Valid ChangeDescriptionRequest request){
    this.commandBus.dispatch(
      CommandIntent.builder(ChangeTaskDescriptionCommand.class.getName())
        .request(request)
        .id(id)
        .build()
    );
  }

  @RequestMapping(value = "/{id}",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public TaskView getTask(@PathVariable("id") DocumentId id){
    return viewStore.findById(id);
  }

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public List<TaskView> listAll(){
    return viewStore.listAll();
  }

  @Data
  public static class CreateRequest{

    @NotNull
    public String description;
  }

  @Data
  public static class ChangeDescriptionRequest{

    @NotNull
    public String description;
  }

}
