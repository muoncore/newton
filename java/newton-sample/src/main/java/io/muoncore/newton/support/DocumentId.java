package io.muoncore.newton.support;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.bson.types.ObjectId;

import java.io.Serializable;

public final class DocumentId implements Serializable, Comparable<DocumentId>/*, AggregateRootId*/ {

	private ObjectId objectId;

	public DocumentId() {
		this.objectId = new ObjectId();
	}

	private DocumentId(String objectId) {
		if (objectId != null) {
			this.objectId = new ObjectId(objectId);
		}
	}

	private DocumentId(ObjectId objectId) {
		this.objectId = objectId;
	}

	@JsonCreator
	public static DocumentId valueOf(String value) {
		return new DocumentId(value);
	}

	public static DocumentId valueOf(ObjectId value) {
		return new DocumentId(value);
	}

	public ObjectId getObjectId() {
		return objectId;
	}

	@JsonValue
	public String getValue() {
		return objectId.toHexString();
	}

	@Override
	public String toString() {
		return objectId != null ? objectId.toHexString() : null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DocumentId that = (DocumentId) o;

		return objectId != null ? objectId.equals(that.objectId) : that.objectId == null;
	}

	@Override
	public int hashCode() {
		return objectId != null ? objectId.hashCode() : 0;
	}

	@Override
	public int compareTo(DocumentId o) {
		if (getObjectId() == null && o.getObjectId() != null) {
			return -1;
		} else if (getObjectId() != null && o.getObjectId() == null) {
			return 1;
		}
		return getObjectId().compareTo(o.getObjectId());
	}
}
