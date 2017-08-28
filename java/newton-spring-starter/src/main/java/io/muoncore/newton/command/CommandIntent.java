package io.muoncore.newton.command;

import io.muoncore.newton.NewtonEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class CommandIntent {

	private String type;
	private Object payload;
	private Object id;
	private Map<String, Object> additionalProperties = new HashMap<>();
	private NewtonEvent causingEvent;

	public static CommandIntentBuilder builder(String type) {
		return new CommandIntentBuilder(type);
	}

	public static class CommandIntentBuilder {

		private String type;
		private Object request;
		private Object id;
		private Map<String, Object> additionalProperties = new HashMap<>();
		private NewtonEvent causingEvent;

		public CommandIntentBuilder(String type) {
			this.type = Objects.requireNonNull(type, "Type is null!");
		}

		public CommandIntentBuilder causedBy(NewtonEvent ev) {
		  this.causingEvent = ev;
		  return this;
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

		public CommandIntent build() {
			return new CommandIntent(type, request, id, additionalProperties, causingEvent);
		}

	}
}
