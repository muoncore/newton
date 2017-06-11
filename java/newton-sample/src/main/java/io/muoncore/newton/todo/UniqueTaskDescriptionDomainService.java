package io.muoncore.newton.todo;

import io.muoncore.newton.EventHandler;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.InMemoryUniqueAggregateDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class UniqueTaskDescriptionDomainService extends InMemoryUniqueAggregateDomainService<String> {

  @Autowired
  public UniqueTaskDescriptionDomainService(StreamSubscriptionManager streamSubscriptionManager) throws IOException {
    super(streamSubscriptionManager);
  }

  @Override
  protected String[] eventStreams() {
    return new String[]{"newton-sample/Task"};
  }

  @EventHandler
  public void handle(TaskCreatedEvent event){
    log.info("Processing event into view {}", event);
    if (event.getDescription().equals("exception")){
      throw new RuntimeException("Unique name exception....");
    }
    addValue(event.getId(),event.getDescription());
  }

}
