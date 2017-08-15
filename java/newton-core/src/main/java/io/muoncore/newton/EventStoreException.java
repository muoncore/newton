package io.muoncore.newton;

import lombok.Getter;

@Getter
public class EventStoreException extends RuntimeException {
	private Object id;

	public EventStoreException(Object id, String stream, String message) {
		super("Unable to load aggregate with ID " + id + " on stream " + stream + ", the event store could not be contacted, the underlying error was reported as:" + message);
		this.id = id;
	}
}
