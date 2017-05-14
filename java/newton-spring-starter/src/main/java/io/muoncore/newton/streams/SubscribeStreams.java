package io.muoncore.newton.streams;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SubscribeStreams {
    String[] value();
}