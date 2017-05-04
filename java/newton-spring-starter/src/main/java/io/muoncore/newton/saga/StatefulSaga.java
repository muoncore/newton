package io.muoncore.newton.saga;

import io.muoncore.newton.*;
import io.muoncore.newton.command.CommandIntent;
import lombok.Getter;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

/**
 * A Saga that maintains its state in a mutable store.
 */
public abstract class StatefulSaga<T extends NewtonEvent> implements Saga<T, AggregateRootId> {

    protected AggregateRootId id = new AggregateRootId();

    @Getter
    private CorrelationId correlationId = new CorrelationId(CorrelationId.CorrelationType.SAGA);
    private long version;
    @Getter
    private boolean complete = false;

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

    public AggregateRootId getId() {
        return id;
    }

    public void setId(AggregateRootId id) {
        this.id = id;
    }

    protected <E extends NewtonEvent> void notifyOn(Class<E> type, String key, String value) {
        newSagaInterests.add(new SagaInterest(getClass().getName(), type.getName(), new SimpleAggregateRootId(), getId(), key, value));
    }

    protected void end() {
        complete = true;
        raiseCommand(CommandIntent.builder(SagaEndCommand.class.getName())
//                .correlate(getCorrelationId())
                .id(getId()).build()
        );
    }

    @Override
    public void handle(NewtonEvent event) {
        boolean eventHandled = eventAdaptor.apply(event);

        if (!eventHandled) {
            throw new IllegalStateException("Undefined domain event handler method for event: ".concat(event.getClass().getName()));
        }
        version++;
    }

}
