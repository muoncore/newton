package io.muoncore.newton.eventsource;

import io.muoncore.newton.NewtonEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents an update to an aggregate root.
 *
 * The Aggregate will be in the state <b>after</b> the given event is applied. The event is the latest event to be applied at this point in the
 * aggregate roots event stream.
 * @param <A>
 */
@AllArgsConstructor
@Getter
public class AggregateRootUpdate<A> {
  private A aggregateRoot;
  private NewtonEvent update;
}
