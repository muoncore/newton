package io.muoncore.newton.eventsource;

import io.muoncore.newton.AggregateRoot;

import java.util.HashMap;
import java.util.Map;


public class AggregateRootUtil {

  private static Map<Class<? extends AggregateRoot>, String> streams = new HashMap<>();

  public static String getAggregateRootStream(Class<? extends AggregateRoot> aggregateRoot) {
    String stream = streams.get(aggregateRoot);
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

    return stream;
  }
}
