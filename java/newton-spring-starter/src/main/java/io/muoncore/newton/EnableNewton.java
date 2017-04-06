package io.muoncore.newton;


import io.muoncore.newton.utils.muon.EnableNewtonRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable auto creation of newton Aggregate Repositories
 *
 * Will use the current
 *
 * Optionally, pass in packages that will be scanned for Aggregates
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableNewtonRegistrar.class)
public @interface EnableNewton {
  String[] value() default {};
}
