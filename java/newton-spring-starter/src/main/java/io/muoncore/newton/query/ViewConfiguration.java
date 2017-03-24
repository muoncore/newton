package io.muoncore.newton.query;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ViewConfiguration {
    String[] streams();
}
