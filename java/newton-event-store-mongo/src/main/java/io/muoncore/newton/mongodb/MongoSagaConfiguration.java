package io.muoncore.newton.mongodb;


import io.muoncore.newton.saga.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoSagaConfiguration {

  @Bean
  public SagaRepository repository(MongoTemplate template) {
    return new MongoSagaRepository(template);
  }

}
