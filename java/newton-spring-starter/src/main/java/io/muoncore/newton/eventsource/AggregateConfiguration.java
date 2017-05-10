package io.muoncore.newton.eventsource;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AggregateConfiguration {
  String context() default "${spring.application.name}";
}
