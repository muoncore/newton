package io.muoncore.newton;


import io.muoncore.newton.command.CommandConfiguration;
import io.muoncore.newton.eventsource.muon.MuonEventSourceConfiguration;
import io.muoncore.newton.query.QueryConfiguration;
import io.muoncore.newton.saga.SagaConfiguration;
import io.muoncore.newton.utils.muon.EnableNewtonRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable auto creation of newton Aggregate Repositories
 * <p>
 * Will use the current
 * <p>
 * Optionally, pass in packages that will be scanned for Aggregates
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {
  EnableNewtonRegistrar.class,
  MuonEventSourceConfiguration.class,
  CommandConfiguration.class,
  QueryConfiguration.class,
  SagaConfiguration.class
})
public @interface EnableNewton {
  String[] value() default {};
}
