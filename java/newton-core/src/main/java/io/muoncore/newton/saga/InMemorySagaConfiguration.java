package io.muoncore.newton.saga;

import io.muoncore.Muon;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.cluster.LockService;
import io.muoncore.newton.cluster.MuonClusterAwareTrackingSubscriptionManager;
import io.muoncore.newton.command.CommandBus;
import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import io.muoncore.newton.query.EventStreamIndexStore;
import io.muoncore.protocol.event.client.EventClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConditionalOnClass(Muon.class)
public class InMemorySagaConfiguration {

  @Bean
  @ConditionalOnMissingBean(SagaRepository.class)
  public SagaRepository sagaRepository() {
    return new InMemorySagaRepository();
  }

  @Bean
  @ConditionalOnMissingBean(SagaBus.class)
  public SagaBus sagaBus(SagaFactory sagaFactory) {
    return new SimpleSagaBus(sagaFactory);
  }

  @Bean
  @ConditionalOnMissingBean(SagaFactory.class)
  public SagaFactory sagaFactory(SagaRepository sagaRepository, CommandBus commandBus) {
    return new SagaFactory(sagaRepository, commandBus);
  }

  @Bean
  @ConditionalOnMissingBean(SagaStreamManager.class)
  public SagaStreamManager sagaStreamManager(StreamSubscriptionManager streamSubscriptionManager,
                                             SagaRepository sagaRepository, CommandBus commandBus,
                                             SagaInterestMatcher sagaInterestMatcher, SagaFactory sagaFactory,
                                             SagaLoader sagaLoader) {
    return new SagaStreamManager(streamSubscriptionManager, sagaRepository, commandBus, sagaInterestMatcher,
                                 sagaFactory, sagaLoader);
  }

  @Bean
  @ConditionalOnMissingBean(StreamSubscriptionManager.class)
  public StreamSubscriptionManager subscriptionManager(EventClient eventClient,
                                                       EventStreamIndexStore eventStreamIndexStore,
                                                       LockService lockService,
                                                       EventStreamProcessor eventStreamProcessor) {
    return new MuonClusterAwareTrackingSubscriptionManager(eventClient, eventStreamIndexStore, lockService,
                                                           eventStreamProcessor);
  }

  @Bean
  @ConditionalOnMissingBean(SagaInterestMatcher.class)
  public SagaInterestMatcher sagaInterestMatcher() {
    return new SagaInterestMatcher();
  }

  @Scope("prototype")
  @Bean
  @ConditionalOnMissingBean(SagaEndCommand.class)
  public SagaEndCommand sagaEndCommand() {
    return new SagaEndCommand();
  }
}
