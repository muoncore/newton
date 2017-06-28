package io.muoncore.newton.todo.composite;

import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.domainservice.EventDrivenDomainService;
import io.muoncore.newton.todo.Task;
import io.muoncore.newton.todo.TaskCreatedEvent;
import io.muoncore.newton.todo.TaskDescriptionChangedEvent;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.client.EventClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@Component
@Slf4j
public class TaskChangeAdaptorService extends EventDrivenDomainService {

  private EventClient eventClient;

  @Autowired
  public TaskChangeAdaptorService(StreamSubscriptionManager streamSubscriptionManager, EventClient eventClient) throws IOException {
    super(streamSubscriptionManager);
    this.eventClient = eventClient;
  }

//  @Override
//  protected String[] eventStreams() {
//    return new String[]{"newton-sample/Task"};
//  }

  @Override
  protected Collection<Class<? extends AggregateRoot>> aggregateRoots() {
    return Collections.singletonList(Task.class);
  }

  @EventHandler
  public void handle(TaskCreatedEvent event) {
    log.info("Handling task description changed....");
    TaskCompositeEvent changedEvent = new TaskCompositeEvent(event.getId(),event.getDescription());
    eventClient.eventAsync(ClientEvent.ofType(changedEvent.getClass().getSimpleName()).stream("newton-sample/Task").payload(changedEvent).build());
    log.info("Task changed event sent");
  }

  @EventHandler
  public void handle(TaskDescriptionChangedEvent event) {
    log.info("Handling task description changed....");
    TaskCompositeEvent changedEvent = new TaskCompositeEvent(event.getId(),event.getDescription());
    eventClient.eventAsync(ClientEvent.ofType(changedEvent.getClass().getSimpleName()).stream("newton-sample/Task").payload(changedEvent).build());
    log.info("Task changed event sent");
  }
}
