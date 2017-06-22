package io.muoncore.newton.eventsource;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.protocol.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventTypeNotFound implements NewtonEvent<Long> {
  private Long id;
  private Event payload;
}
