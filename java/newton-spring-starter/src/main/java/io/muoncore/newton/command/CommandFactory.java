package io.muoncore.newton.command;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

//todo: addtional validations:
// 1. ensure Command class is public (silently fails ons setter)
// 2. ensure class found on classpath
public class CommandFactory implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  public Command create(Class<? extends Command> commandType) {
    return create(commandType, null, null, null, null);
  }

  public Command create(Class<? extends Command> commandType, Object payload, String tenantId) {
    return create(commandType, payload, null, null, tenantId);
  }

  public Command create(Class<? extends Command> commandType, Object payload, Object id, String tenantId) {
    return create(commandType, payload, id, null, tenantId);
  }

  public Command create(Class<? extends Command> commandType, Object payload, Object id, Map<String, Object> additionalProperties, String tenantId) {
    Command command = loadFromSpringContext(commandType);
    if (payload != null) {
      command = decorateWithPayload(command, payload);
    }
    if (id != null) {
      command = decorateWithId(command, id);
    }
    if (command instanceof TenantedCommand) {
      if (tenantId == null) throw new CommandCreateException(String.format("Unable to create command '' as an instance of TenantedCommand was passed through, but no tenantId is available!",commandType.getName()));
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
      BeanUtils.populate(cmd, additionalProperties);

//      for (String key : additionalProperties.keySet()) {
//        final Method method = cmd.getClass().getDeclaredMethod("set".concat(StringUtils.capitalize(key)), additionalProperties.get(key).getClass());
//        method.invoke(cmd, additionalProperties.get(key));
//      }
      return cmd;
    } catch (Exception e) {
      throw new CommandCreateException(String.format("Unable to decorate command '%s' with with additional properties", cmd.getClass().getName()), e);
    }
  }

  private Command loadFromSpringContext(Class<? extends Command> commandType) {
    try {
      return applicationContext.getBean(commandType);
    } catch (BeansException e) {
      throw new CommandCreateException(String.format("Unable to load command '%s' from spring application context", commandType.getName()), e);
    }
  }

  private Command decorateWithPayload(Command command, Object payload) {
    if (payload != null) {
      try {
        BeanUtilsBean2.getInstance().copyProperties(command, payload);
      } catch (Exception e) {
        throw new CommandCreateException(String.format("Unable to decorate command '%s' with payload specified", command.getClass().getName()), e);
      }
    }
    return command;
  }

  private Command decorateWithId(Command command, Object id) {

    try {
      PropertyDescriptor id1 = PropertyUtils.getPropertyDescriptor(command, "id");;
      if (id1 != null) {
        PropertyUtils.setProperty(command, "id", id);
      }
    } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      throw new CommandCreateException(String.format("Unable to assign id property to command '%s'", command.getClass().getName()), e);
    }

    return command;
  }

}
