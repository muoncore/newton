package io.muoncore.newton.query;

import io.muoncore.Muon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Muon.class)
public class InMemoryQueryConfiguration {

  @Bean
  @ConditionalOnMissingBean(EventStreamIndexStore.class)
  public EventStreamIndexStore indexStore() {
    return new InMemEventStreamIndexStore();
  }

}
