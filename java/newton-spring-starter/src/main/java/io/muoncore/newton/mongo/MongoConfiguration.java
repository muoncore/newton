package io.muoncore.newton.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import java.util.Arrays;

@Configuration
@ConditionalOnClass(MongoClient.class)
@Import(value = MongoAutoConfiguration.class)
public class MongoConfiguration extends AbstractMongoConfiguration {

  @Autowired
  private Mongo mongo;

  @Autowired
  private MongoProperties mongoProperties;

  @Override
  protected String getDatabaseName() {
    return mongoProperties.getDatabase();
  }

  @Override
  public Mongo mongo() throws Exception {
    return mongo;
  }

  @Bean
  //TODO: workaround so that the default mongo template is not loaded if multi-tenancy enabled & marked as @Primary (See MultiTenancyConfiguration)
  @ConditionalOnExpression("#{systemProperties['app.multi_tenancy.support.enabled'] == null ||  systemProperties['app.multi_tenancy.support.enabled'] == false}")
  public MongoTemplate mongoTemplate() throws Exception {
    return new MongoTemplate(this.mongoDbFactory(), this.mappingMongoConverter());
  }

  @Override
  public CustomConversions customConversions() {
    return new CustomConversions(Arrays.asList(
      new StringToAggregateRootId(),
      new AggregateRootIdToStirng()
    ));
  }
}
