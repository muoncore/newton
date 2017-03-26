package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class SagaInterestMatcher {

    public boolean matches(NewtonEvent event, SagaInterest interests) {
        final Method[] methods = event.getClass().getDeclaredMethods();

        boolean matches = false;

        for (Method method : methods) {
            if (method.getName().startsWith("get")) {
                PropertyDescriptor ed = BeanUtils.findPropertyForMethod(method);
                if (ed == null) {
                  log.debug("No propertyDescriptor for {}", method.getName());
                  continue;
                }
                log.debug("Pre Matching {} against {}", ed, interests);
                log.debug("Matching {} against {}", ed.getName(), interests.getKey());
                if (ed.getName().equals(interests.getKey())) {
                    try {
                        Object ret = method.invoke(event);
                        String val = ret.toString();
                        if (interests.getValue().equals(val)) {
                            matches = true;
                            break;
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return matches;
    }
}
