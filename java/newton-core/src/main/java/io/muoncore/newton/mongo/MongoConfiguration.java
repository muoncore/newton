package io.muoncore.newton.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import io.muoncore.newton.query.EventStreamIndexStore;
import io.muoncore.newton.query.mongo.MongoEventStreamIndexStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
//@ConditionalOnMissingBean(value = {Mongo.class})
//@ConditionalOnClass(MongoClient.class)
@Import(value = MongoAutoConfiguration.class)
public class MongoConfiguration extends AbstractMongoConfiguration {

  @Autowired
  private Mongo mongo;

  @Autowired
  private MongoProperties mongoProperties;

  @Override
  protected String getDatabaseName() {
    return "test";
  }

  @Override
  public Mongo mongo() throws Exception {
    return mongo;
  }

  @Bean
  public MongoTemplate mongoTemplate() throws Exception {
    return new MongoTemplate(this.mongoDbFactory(), this.mappingMongoConverter());
  }

  @Bean
  @ConditionalOnClass(MongoTemplate.class)
  @ConditionalOnMissingBean(EventStreamIndexStore.class)
  public EventStreamIndexStore indexStore(MongoTemplate mongoTemplate) {
    return new MongoEventStreamIndexStore(mongoTemplate);
  }
}
