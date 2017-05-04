package io.muoncore.newton.command;

import io.muoncore.newton.AggregateRootId;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;

public class CommandFactory implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public Command create(Class<? extends Command> commandType) {
		return create(commandType, null, null, null, null);
	}

	public Command create(Class<? extends Command> commandType, Object payload, String tenantId) {
		return create(commandType, payload, null, null, tenantId);
	}

	public Command create(Class<? extends Command> commandType, Object payload, AggregateRootId id, String tenantId) {
		return create(commandType, payload, id, null, tenantId);
	}

	public Command create(Class<? extends Command> commandType, Object payload, AggregateRootId id, Map<String, Object> additionalProperties, String tenantId) {
		Command command = loadFromSpringContext(commandType);
		if (payload != null) {
			command = decorateWithPayload(command, payload);
		}
		if (id != null) {
			command = decorateWithId(command, id);
		}
		if (command instanceof TenantedCommand) {
			if (tenantId == null) throw new IllegalArgumentException("An instance of TenantedCommand was passed through, but no tenantId is available, this is an error");
			((TenantedCommand) command).setTenantId(tenantId);
		}
		if (additionalProperties != null && additionalProperties.size() > 0) {
			command = decorateWithAdditionalProperties(command, additionalProperties);
		}
		return command;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private Command decorateWithAdditionalProperties(Command cmd, Map<String, Object> additionalProperties) {
		try {
			if (additionalProperties.isEmpty()) {
				return cmd;
			}
			for (String key : additionalProperties.keySet()) {
				final Method method = cmd.getClass().getDeclaredMethod("set".concat(StringUtils.capitalize(key)), additionalProperties.get(key).getClass());
				method.invoke(cmd, additionalProperties.get(key));
			}
			return cmd;
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to assign additional properties to Command: ".concat(cmd.getClass().getSimpleName()), e);
		}
	}

	private Command loadFromSpringContext(Class commandType) {
		return (Command) applicationContext.getBean(commandType);
	}

	private Command decorateWithPayload(Command command, Object payload) {
		if (payload != null) {
			try {
				BeanUtils.copyProperties(command, payload);
			} catch (Exception e) {
				throw new IllegalStateException("Unable to create command: ".concat(command.getClass().getName()), e);
			}
		}
		return command;
	}

	private Command decorateWithId(Command command, AggregateRootId id) {
		if (command instanceof IdentifiableCommand) {
			((IdentifiableCommand) command).setId(id);
		}
		return command;
	}
}
