package io.muoncore.newton.eventsource.muon;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.newton.AggregateEventClient;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.cluster.*;
import io.muoncore.newton.query.EventStreamIndexStore;
import io.muoncore.newton.saga.SagaLoader;
import io.muoncore.protocol.event.client.DefaultEventClient;
import io.muoncore.protocol.event.client.EventClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConditionalOnClass(Muon.class)
public class MuonEventSourceConfiguration {

  @Value("${spring.application.name}")
  private String applicationName;

  @Bean
  @Profile("!test")
  public Muon muon(Codecs codecs, @Value("${muon.amqp.url}") String amqpUrl){
    AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier(applicationName)
      .addWriter(autoConfiguration -> {
        autoConfiguration.getProperties().put("amqp.transport.url", amqpUrl);
        autoConfiguration.getProperties().put("amqp.discovery.url", amqpUrl);
      }).build();

    return MuonBuilder.withConfig(config).withCodecs(codecs).build();
  }

  @ConditionalOnMissingBean(Codecs.class)
  @Bean
  public Codecs codecs() {
    return new JsonOnlyCodecs();
  }

  @Bean
  @ConditionalOnMissingBean(EventStreamProcessor.class)
  public EventStreamProcessor eventStreamProcessor() {
    return new NoOpEventStreamProcessor();
  }

  @Bean
  public EventClient eventClient(Muon muon) {
    return new DefaultEventClient(muon);
  }

  @Bean
  public AggregateEventClient aggregateEventClient(EventClient eventClient) {
    return new AggregateEventClient(eventClient);
  }

  @ConditionalOnMissingBean(StreamSubscriptionManager.class)
  @Bean
  public StreamSubscriptionManager subscriptionManager(EventClient eventClient, EventStreamIndexStore eventStreamIndexStore, LockService lockService, EventStreamProcessor eventStreamProcessor) {
    return new MuonClusterAwareTrackingSubscriptionManager(eventClient, eventStreamIndexStore, lockService, eventStreamProcessor);
  }

  @ConditionalOnMissingBean(LockService.class)
  @Bean
  public LockService lockService() throws Exception {
    return new LocalOnlyLockService();
  }

  @ConditionalOnMissingBean(SagaLoader.class)
  @Bean
  public SagaLoader sagaLoader() {
    return interest -> (Class) Class.forName(interest.getSagaClassName());
  }


}


