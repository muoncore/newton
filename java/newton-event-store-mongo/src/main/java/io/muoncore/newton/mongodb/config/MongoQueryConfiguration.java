package io.muoncore.newton.mongodb.config;

import io.muoncore.Muon;
import io.muoncore.newton.query.EventStreamIndexStore;
import io.muoncore.newton.mongodb.MongoEventStreamIndexStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@ConditionalOnClass(Muon.class)
public class MongoQueryConfiguration {

  @Bean
  @ConditionalOnClass(MongoTemplate.class)
  @ConditionalOnMissingBean(EventStreamIndexStore.class)
  public EventStreamIndexStore indexStore(MongoTemplate mongoTemplate) {
    return new MongoEventStreamIndexStore(mongoTemplate);
  }

}
