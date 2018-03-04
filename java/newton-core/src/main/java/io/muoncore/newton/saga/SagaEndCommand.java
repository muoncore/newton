package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEventClient;
import io.muoncore.newton.command.Command;
import io.muoncore.newton.saga.events.SagaEndEvent;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventResult;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static io.muoncore.newton.saga.SagaStreamManager.SAGA_LIFECYCLE_STREAM;

@Slf4j
public class SagaEndCommand implements Command {

  @Setter
  private String id;

  @Autowired
  transient private NewtonEventClient eventClient;

  @Override
  public void execute() {
    log.debug("Saga " + id + " is ending, sending SagaEnd lifecycle event.");

    EventResult result = eventClient.event(ClientEvent.ofType(SagaEndEvent.class.getSimpleName())
                                                      .stream(SAGA_LIFECYCLE_STREAM)
                                                      .payload(new SagaEndEvent(id))
                                                      .build()
    );

    if (result.getStatus() == EventResult.EventResultStatus.FAILED) {
      log.error("SagaEnd event not accepted by the event store");
    }

    log.debug("Sent end saga lifecycle event - " + result.getCause());
  }
}
