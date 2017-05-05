package io.muoncore.newton.todo;

import io.muoncore.newton.EventHandler;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.UniqueAggregateDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UniqueTodoDescriptionDomainService extends UniqueAggregateDomainService<String> {

  @Autowired
  public UniqueTodoDescriptionDomainService(StreamSubscriptionManager streamSubscriptionManager) throws IOException {
    super(streamSubscriptionManager, Todo.class);
  }

  @EventHandler
  public void handle(TodoCreatedEvent event){
    if (event.getDescription().equals("exception")){
      throw new RuntimeException("Unique name exception....");
    }
    addValue(event.getId(),event.getDescription());
  }

}
