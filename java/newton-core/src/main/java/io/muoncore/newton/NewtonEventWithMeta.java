package io.muoncore.newton;

import io.muoncore.protocol.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public abstract class NewtonEventWithMeta<T> implements NewtonEvent<T> {
  @Getter @Setter
  private transient Event meta;
}
