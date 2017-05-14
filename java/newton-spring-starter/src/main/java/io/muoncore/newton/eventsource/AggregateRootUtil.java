package io.muoncore.newton.eventsource;

import io.muoncore.newton.AggregateRoot;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class AggregateRootUtil {

  private static Map<Class<? extends AggregateRoot>, String> streams = new HashMap<>();
  private static final Set<Class> configuredRoots = Collections.synchronizedSet(new HashSet<>());

  public static String getAggregateRootStream(Class<? extends AggregateRoot> aggregateRoot) {
    while(!configuredRoots.contains(aggregateRoot)) {
      try {
        synchronized (configuredRoots) {
          configuredRoots.wait(100);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    String stream = streams.get(aggregateRoot);
    log.debug("{} has stream {}", aggregateRoot.getName(), stream);
    if(stream == null) throw new IllegalStateException(
      String.format("AggregateRoot %s has not been initialised by the registrar and its stream can't be determined. This is an error and cannot be remedied.", aggregateRoot.getName()));

    return stream;
  }

  public static String getAggregateRootStream(Class<? extends AggregateRoot> aggregateRoot, String defaultContext) {

    AggregateConfiguration a = aggregateRoot.getAnnotation(AggregateConfiguration.class);

    String context;

    if (a != null) {
      context = a.context();
    } else {
      context = defaultContext;
    }

    String stream = context.concat("/").concat(aggregateRoot.getSimpleName());

    streams.put(aggregateRoot, stream);

    synchronized (configuredRoots) {
      configuredRoots.add(aggregateRoot);
      configuredRoots.notifyAll();
    }

    return stream;
  }
}
