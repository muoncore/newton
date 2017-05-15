package io.muoncore.newton.streams;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Deprecated
@StreamSubscriptionConfig(type= StreamSubscriptionType.GLOBAL_LOCK)
@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventDrivenDomainService {
}
