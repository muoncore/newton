package io.muoncore.newton.query;

import io.muoncore.Muon;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.query.mongo.MongoEventStreamIndexStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@ConditionalOnClass(Muon.class)
public class QueryConfiguration {

	@Bean
	@ConditionalOnClass(MongoTemplate.class)
  @ConditionalOnMissingBean(EventStreamIndexStore.class)
	public EventStreamIndexStore indexStore(MongoTemplate mongoTemplate) {
		return new MongoEventStreamIndexStore(mongoTemplate);
	}

}
