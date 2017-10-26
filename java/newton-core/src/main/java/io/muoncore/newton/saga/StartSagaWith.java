package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StartSagaWith {
}
