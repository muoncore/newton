package io.muoncore.newton.eventsource;

import lombok.Getter;

@Getter
public class OptimisticLockException extends RuntimeException {
	private Object id;
	private Long version;
	private Long actualVersion;

	public OptimisticLockException(Object id, Long version, Long actual) {
		super("Modification detected on aggregate id " + id + ", expected version [" + version + "] actual version [" + actual + "]");
	}

}
