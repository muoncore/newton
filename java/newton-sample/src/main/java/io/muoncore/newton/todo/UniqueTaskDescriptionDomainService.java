package io.muoncore.newton.todo;

import io.muoncore.newton.EventHandler;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.UniqueAggregateDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UniqueTaskDescriptionDomainService extends UniqueAggregateDomainService<String> {

  @Autowired
  public UniqueTaskDescriptionDomainService(StreamSubscriptionManager streamSubscriptionManager) throws IOException {
    super(streamSubscriptionManager);
  }

  @Override
  protected String[] eventStreams() {
    return new String[]{"newton-sample/Todo"};
  }

  @EventHandler
  public void handle(TaskCreatedEvent event){
    if (event.getDescription().equals("exception")){
      throw new RuntimeException("Unique name exception....");
    }
    addValue(event.getId(),event.getDescription());
  }

}
