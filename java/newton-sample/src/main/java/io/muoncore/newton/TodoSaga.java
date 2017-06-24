package io.muoncore.newton;

import io.muoncore.newton.saga.SagaStreamConfig;
import io.muoncore.newton.saga.StartSagaWith;
import io.muoncore.newton.saga.StatefulSaga;
import io.muoncore.newton.todo.Task;
import io.muoncore.newton.todo.TaskCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component
@SagaStreamConfig(aggregateRoots = {Task.class})
@Slf4j
public class TodoSaga extends StatefulSaga {
  @StartSagaWith
  public void start(TaskCreatedEvent event) {
    log.info("Starting a new TODO saga and ending");
//    end();
  }
}
