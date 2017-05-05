package io.muoncore.newton.todo;

import io.muoncore.newton.AggregateRootId;
import io.muoncore.newton.command.CommandBus;
import io.muoncore.newton.command.CommandIntent;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.UUID;

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
    final AggregateRootId id = new AggregateRootId(UUID.randomUUID().toString());
    this.commandBus.dispatch(
      CommandIntent.builder(CreateTodoCommand.class.getName())
        .request(createRequest)
        .id(id)
        .build()
    );
    final URI location = UriComponentsBuilder.fromPath("/api/todos/".concat(id.getValue())).build().toUri();
    return ResponseEntity.created(location).build();
  }

  @Data
  public static class CreateRequest{

    @NotNull
    public String description;
  }
}
