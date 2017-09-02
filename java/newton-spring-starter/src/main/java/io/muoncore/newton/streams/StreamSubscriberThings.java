package io.muoncore.newton.streams;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

@Deprecated
public class StreamSubscriberThings {

  public static void main(String[] args) {
    new StreamSubscriberThings().findAnnotatedClasses("io.muoncore.newton");
  }

  public void findAnnotatedClasses(String scanPackage) {
    ClassPathScanningCandidateComponentProvider provider = createComponentScanner();
    for (BeanDefinition beanDef : provider.findCandidateComponents(scanPackage)) {
      printMetadata(beanDef);
    }
  }

  private ClassPathScanningCandidateComponentProvider createComponentScanner() {
    // Don't pull default filters (@Component, etc.):
    ClassPathScanningCandidateComponentProvider provider
      = new ClassPathScanningCandidateComponentProvider(false);
    provider.addIncludeFilter(new AnnotationTypeFilter(StreamSubscriptionConfig.class));
    return provider;
  }

  private void printMetadata(BeanDefinition beanDef) {
    try {
      Class<?> cl = Class.forName(beanDef.getBeanClassName());
      StreamSubscriptionConfig findable = cl.getAnnotation(StreamSubscriptionConfig.class);
      System.out.printf("Found class: %s, with meta name: %s%n",
        cl.getSimpleName(), findable.type());
    } catch (Exception e) {
      System.err.println("Got exception: " + e.getMessage());
    }
  }
}
