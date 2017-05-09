package io.muoncore.newton;

public interface NewtonEvent<A extends AggregateRootId> {
  A getId();
}
