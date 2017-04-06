package io.muoncore.newton.mongo;

import io.muoncore.newton.DocumentId;
import org.bson.types.ObjectId;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.data.convert.ReadingConverter;

import java.util.Collections;
import java.util.Set;

@ReadingConverter
//ObjectId -> DocumentId
public class DocumentIdReadConverter implements ConditionalGenericConverter {

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(ObjectId.class, DocumentId.class));
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return sourceType.getType().equals(ObjectId.class) && DocumentId.class.isAssignableFrom(targetType.getObjectType());
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}
		return DocumentId.valueOf((ObjectId) source);
	}
}
