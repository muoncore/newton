package io.muoncore.newton.saga;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaStreamConfig {
    String[] streams();
}
