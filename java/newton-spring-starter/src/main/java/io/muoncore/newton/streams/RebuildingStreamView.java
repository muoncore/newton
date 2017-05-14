package io.muoncore.newton.streams;

import io.muoncore.newton.streams.StreamSubscriptionConfig;
import io.muoncore.newton.streams.StreamSubscriptionType;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Deprecated
@StreamSubscriptionConfig(type= StreamSubscriptionType.LOCAL)
@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RebuildingStreamView {
}
