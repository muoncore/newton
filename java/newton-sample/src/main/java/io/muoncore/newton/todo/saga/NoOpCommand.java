package io.muoncore.newton.todo.saga;

import io.muoncore.newton.command.Command;
import io.muoncore.newton.support.DocumentId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Slf4j
@Component
@Scope(SCOPE_PROTOTYPE)
public class NoOpCommand implements Command {

  private DocumentId id;

  @Override
  public void execute() {
      log.info("Excecuting NO-OP command on: {}", this.id);
  }
}
