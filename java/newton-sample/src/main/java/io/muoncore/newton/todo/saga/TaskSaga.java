package io.muoncore.newton.todo.saga;

import io.muoncore.newton.EventHandler;
import io.muoncore.newton.command.CommandIntent;
import io.muoncore.newton.saga.SagaStreamConfig;
import io.muoncore.newton.saga.StartSagaWith;
import io.muoncore.newton.saga.StatefulSaga;
import io.muoncore.newton.todo.Task;
import io.muoncore.newton.todo.TaskCreatedEvent;
import io.muoncore.newton.todo.TaskDescriptionChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Scope("prototype")
@Component
@SagaStreamConfig(aggregateRoots = {Task.class})
@Slf4j
public class TaskSaga extends StatefulSaga {

	public TaskSaga() {
	}

	@StartSagaWith
	public void start(TaskCreatedEvent event) {
		log.debug("Starting saga");
		notifyOn(TaskDescriptionChangedEvent.class, "id", event.getId().toString());

		log.debug("Dispatch add team to policy command");
		Map<String, Object> cmdProps = new HashMap<>();
		cmdProps.put("tenantId", event.getTenantId());
		cmdProps.put("teamId", event.getId());

		raiseCommand(CommandIntent.builder(NoOpCommand.class.getName())
			.id(event.getId())
			.build()
		);

	}

	@EventHandler
	public void handle(TaskDescriptionChangedEvent event) {
		log.debug("Ending saga");
		end();
	}
}
