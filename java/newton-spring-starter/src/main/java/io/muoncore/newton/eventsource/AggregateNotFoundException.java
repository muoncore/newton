package io.muoncore.newton.eventsource;

import lombok.Getter;

@Getter
public class AggregateNotFoundException extends RuntimeException {
	private Object id;

	public AggregateNotFoundException(Object id) {
		super("Unable to load aggregate with ID " + id + ", it does not exist in the event store");
		this.id = id;
	}
}
