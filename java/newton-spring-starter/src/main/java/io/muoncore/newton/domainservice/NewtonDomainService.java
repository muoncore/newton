package io.muoncore.newton.domainservice;

import io.muoncore.newton.AggregateRoot;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NewtonDomainService {

    String[] streams() default {};

    Class<? extends AggregateRoot>[] aggregateRoot()  default {};
}
