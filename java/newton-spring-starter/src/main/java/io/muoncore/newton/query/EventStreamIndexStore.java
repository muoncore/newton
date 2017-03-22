package io.muoncore.newton.query;

import java.util.Optional;

public interface EventStreamIndexStore {

	Optional<EventStreamIndex> findOneById(String id);

	void save(EventStreamIndex eventStreamIndex);
}
