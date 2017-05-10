package io.muoncore.newton.saga;

import io.muoncore.newton.*;
import io.muoncore.newton.command.CommandIntent;
import lombok.Getter;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A Saga that maintains its state in a mutable store.
 */
public abstract class StatefulSaga implements Saga {

    @Getter
    protected String id = UUID.randomUUID().toString();

    private long version;
    @Getter
    private boolean complete = false;

    @Transient
    private transient DynamicInvokeEventAdaptor startEventAdaptor = new DynamicInvokeEventAdaptor(this, StartSagaWith.class);

    @Transient
    private transient DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, EventHandler.class);

    @Transient
    @Getter
    private transient List<CommandIntent> newOperations = new ArrayList<>();

    @Getter
    @Transient
    private transient List<SagaInterest> newSagaInterests = new ArrayList<>();

    protected void raiseCommand(CommandIntent command) {
        newOperations.add(command);
    }

    public long getVersion() {
        return version;
    }

    protected <E extends NewtonEvent> void notifyOn(Class<E> type, String key, String value) {
        newSagaInterests.add(new SagaInterest(getClass().getName(), type.getName(), UUID.randomUUID().toString(), getId(), key, value));
    }

    protected void end() {
        complete = true;
        raiseCommand(CommandIntent.builder(SagaEndCommand.class.getName())
                .id(getId()).build()
        );
    }

    @Override
    public void startWith(NewtonEvent event) {
      boolean eventHandled = startEventAdaptor.apply(event);

      if (!eventHandled) {
        throw new IllegalStateException("Undefined @SagaStartWith event handler method for event: ".concat(event.getClass().getName()));
      }
      version++;
    }

    @Override
    public void handle(NewtonEvent event) {
        boolean eventHandled = eventAdaptor.apply(event);

        if (!eventHandled) {
            throw new IllegalStateException("Undefined @EventHandler method for event: ".concat(event.getClass().getName()));
        }
        version++;
    }

}
