package io.muoncore.newton.mongo;

import mu.cibecs.common.identity.DocumentId;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class DocumentIdWriteConverter implements Converter<DocumentId, ObjectId> {

	@Override
	public ObjectId convert(DocumentId source) {
		return source.getValue();
	}
}
