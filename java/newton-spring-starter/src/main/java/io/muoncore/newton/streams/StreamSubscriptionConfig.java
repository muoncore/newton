package io.muoncore.newton.streams;

import java.lang.annotation.*;

@Deprecated
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StreamSubscriptionConfig {
  StreamSubscriptionType type();
}
