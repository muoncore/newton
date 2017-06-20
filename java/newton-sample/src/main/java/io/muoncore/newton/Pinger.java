package io.muoncore.newton;


import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class Pinger {

  @Autowired
  EventClient eventClient;

  @EventListener
  public void onApplicationEvent(ApplicationReadyEvent onReadyEvent) {
    new Thread(() -> {

      while(true) {
        try {
          EventResult event = eventClient.event(
            ClientEvent.ofType("HelloWorld")
              .stream("mystream")
              .payload(Collections.singletonMap("time", System.currentTimeMillis())).build()
          );

          if (event.getStatus() == EventResult.EventResultStatus.PERSISTED) {
            log.info("Event persisted with orider id {}", event.getOrderId());
          } else {
            log.info("Event failed with cause {}", event.getCause());
          }
          Thread.sleep(2000);
        } catch(Exception e) {
          log.error("Failed while emitting events", e);
        }
      }
    }).start();
  }

}
