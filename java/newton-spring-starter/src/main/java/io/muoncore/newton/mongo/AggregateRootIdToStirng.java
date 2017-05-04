package io.muoncore.newton.mongo;

import io.muoncore.newton.AggregateRootId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class AggregateRootIdToStirng implements Converter<AggregateRootId, String> {
  @Override
  public String convert(AggregateRootId source) {
    return source.getValue();
  }
}
