package io.muoncore.newton.todo;

import io.muoncore.newton.command.CommandBus;
import io.muoncore.newton.command.CommandIntent;
import io.muoncore.newton.support.DocumentId;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

  private CommandBus commandBus;

  @Autowired
  public TodoController(CommandBus commandBus) {
    this.commandBus = commandBus;
  }

  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> create(@RequestBody @Valid CreateRequest createRequest){
    final DocumentId id = new DocumentId();
    this.commandBus.dispatch(
      CommandIntent.builder(CreateTodoCommand.class.getName())
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
      CommandIntent.builder(ChangeTodoDescriptionCommand.class.getName())
        .request(request)
        .id(id)
        .build()
    );
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
