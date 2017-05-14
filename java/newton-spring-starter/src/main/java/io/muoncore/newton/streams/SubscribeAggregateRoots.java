package io.muoncore.newton.streams;

import io.muoncore.newton.AggregateRoot;

import java.lang.annotation.*;

@Deprecated
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SubscribeAggregateRoots {
    Class<? extends AggregateRoot>[] value();
}
