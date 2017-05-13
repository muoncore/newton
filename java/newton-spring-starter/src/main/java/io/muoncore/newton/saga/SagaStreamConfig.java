package io.muoncore.newton.saga;

import io.muoncore.newton.AggregateRoot;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaStreamConfig {
  String[] streams() default {};
  Class<? extends AggregateRoot>[] aggregateRoot()  default {};
}
