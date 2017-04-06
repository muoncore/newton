package io.muoncore.newton.mongo;

import io.muoncore.newton.DocumentId;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * In DAO conversion of DocumentID to ObjectID for aggregation is as a result of
 * spring bug https://jira.spring.io/browse/DATAMONGO-1175 this should be do-able
 * using a Codec wired into the MongoTemplate->MongoClient->MongoClientOptions->CodecRegistry
 * hierarchy.
 * <p>
 * TODO: Monitor bug and replace workaround with codec implementation.
 */
public class DocumentIdToObjectIdConverter {

	public static Collection<ObjectId> convert(DocumentId... documentIds) {
		return Arrays.stream(documentIds).map(DocumentId::getValue).collect(Collectors.toList());
	}

	public static Collection<ObjectId> convert(Collection<DocumentId> documentIds) {
		return convert(documentIds.toArray(new DocumentId[0]));
	}
}
