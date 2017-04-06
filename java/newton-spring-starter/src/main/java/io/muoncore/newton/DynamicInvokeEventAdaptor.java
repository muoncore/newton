package io.muoncore.newton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Dynamic Dispatcher. Will select a method based on the param type and the presence of @OnDomainEvent
 * Will process methods from the class hierarchy.
 */
public class DynamicInvokeEventAdaptor implements Function<NewtonEvent, Boolean>, Consumer<NewtonEvent> {

    private Object delegate;
    private Class<? extends Annotation> annotation;

    public DynamicInvokeEventAdaptor(Object target, Class<? extends Annotation> annotation) {
        this.delegate = target;
        this.annotation = annotation;
    }

    @Override
    public void accept(NewtonEvent event) {
        apply(event);
    }

    @Override
    public Boolean apply(NewtonEvent event) {

        final Method[] methods = delegate.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("lambda$") || !method.isAnnotationPresent(annotation)) {
                continue;
            }
            final Class<?>[] parameterTypes = method.getParameterTypes();
            for (Class<?> parameterType : parameterTypes) {
                if (parameterType.isAssignableFrom(event.getClass())) {
                    try {
                        method.invoke(delegate, event);
                        return true;
                    } catch (Exception e) {
                        throw new IllegalStateException("Unable to handle event: ".concat(event.getClass().getName()), e);
                    }
                }
            }
        }
        return false;
    }
}
