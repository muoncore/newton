package io.muoncore.newton.saga;

import java.lang.annotation.*;

/**
 * See @{@link SagaStreamManager} for info on sagas.
 *
 * All sagas should have at least one method marked with this annotation that accepts a single parameter extending @{@link io.muoncore.newton.NewtonEvent}.
 * This will indicate that the workflow will commence when the event is observed on the streams the saga is connected to
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StartSagaWith {
}
