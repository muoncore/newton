package io.muoncore.newton.command;

import io.muoncore.newton.CorrelationId;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class CommandIntent {

	private String type;
	private Object payload;
	private CorrelationId correlationId;
	private Object id;
	private Map<String, Object> additionalProperties = new HashMap<>();

	public CommandIntent(String type, Object id, Object payload, Map<String, Object> additionalProperties, CorrelationId correlationId) {
		this.type = type;
		this.id = id;
		this.payload = payload;
		this.additionalProperties = additionalProperties;
		this.correlationId = correlationId;
	}

	public static CommandIntentBuilder builder(String type) {
		return new CommandIntentBuilder(type);
	}

	public static class CommandIntentBuilder {

		private String type;
		private Object request;
		private Object id;
		private CorrelationId correlationId;
		private Map<String, Object> additionalProperties = new HashMap<>();

		public CommandIntentBuilder(String type) {
			this.type = Objects.requireNonNull(type, "Type is null!");
		}

		public CommandIntentBuilder request(Object request) {
			this.request = Objects.requireNonNull(request, "Payload is null!");
			return this;
		}

		public CommandIntentBuilder id(Object id) {
			this.id = Objects.requireNonNull(id, "Id is null!");
			return this;
		}

		public CommandIntentBuilder applyProperties(Map<String, Object> properties) {
			this.additionalProperties = Objects.requireNonNull(properties, "Properties is null!");
			return this;
		}

//		public CommandIntentBuilder correlate(CorrelationId correlationId) {
//			this.correlationId = correlationId;
//			return this;
//		}

		public CommandIntent build() {
			return new CommandIntent(type, id, request, additionalProperties, correlationId);
		}

	}
}
