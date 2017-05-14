package io.muoncore.newton.streams;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Deprecated
@StreamSubscriptionConfig(type= StreamSubscriptionType.GLOBAL_LOCK)
@Scope("prototype")
@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Saga {
}
