package io.muoncore.newton.query.mongo;

import io.muoncore.newton.query.EventStreamIndexStore;
import io.muoncore.newton.query.EventStreamIndex;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

//todo: get rid of Mongo store & replace with in-memory or prompt user to provide 1
public class MongoEventStreamIndexStore implements EventStreamIndexStore {

	private MongoTemplate mongoTemplate;

	public MongoEventStreamIndexStore(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}


	@Override
	public Optional<EventStreamIndex> findOneById(String id) {
		return Optional.ofNullable(mongoTemplate.findById(id, EventStreamIndex.class));
	}

	@Override
	public void save(EventStreamIndex eventStreamIndex) {
		mongoTemplate.save(eventStreamIndex);
	}
}
