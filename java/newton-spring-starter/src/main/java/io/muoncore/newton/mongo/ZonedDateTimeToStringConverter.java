package io.muoncore.newton.mongo;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.ZonedDateTime;

@WritingConverter
public class ZonedDateTimeToStringConverter implements Converter<ZonedDateTime, String> {

	@Override
	public String convert(ZonedDateTime source) {
		return source.toString();
	}
}
