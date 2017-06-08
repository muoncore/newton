package io.muoncore.newton;

import io.muoncore.newton.eventsource.AggregateDeletedEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot<T> {

  @Getter
  private boolean deleted;
	private long version;
	private transient DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, EventHandler.class);

	private transient List<NewtonEvent> newOperations = new ArrayList<>();

	protected void raiseEvent(NewtonEvent event) {
		newOperations.add(event);
		handleEvent(event);
	}

	public abstract T getId();

	public long getVersion() {
		return version;
	}

	public List<NewtonEvent> getNewOperations() {
		return newOperations;
	}

	public void handleEvent(NewtonEvent event) {

	  if (event instanceof AggregateDeletedEvent) {
	    deleted = true;
	    version++;
	    return;
    }

		boolean eventHandled = eventAdaptor.apply(event);

		if (!eventHandled) {
			throw new IllegalStateException("Undefined domain event handler method for event: ".concat(event.getClass().getName()));
		}
		version++;
	}
}
