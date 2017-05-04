package io.muoncore.newton.mongo;

import io.muoncore.newton.AggregateRootId;
import io.muoncore.newton.SimpleAggregateRootId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StringToAggregateRootId implements Converter<String, AggregateRootId> {
  @Override
  public AggregateRootId convert(String source) {
    return new SimpleAggregateRootId(source);
  }
}
