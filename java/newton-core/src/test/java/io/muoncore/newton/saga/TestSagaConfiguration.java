package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEventClient;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.cluster.LockService;
import io.muoncore.newton.cluster.MuonClusterAwareTrackingSubscriptionManager;
import io.muoncore.newton.command.CommandBus;
import io.muoncore.newton.eventsource.muon.EventStreamProcessor;
import io.muoncore.newton.query.EventStreamIndexStore;
import io.muoncore.protocol.event.client.EventClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class TestSagaConfiguration {

  @Bean
  public SagaBus sagaBus(SagaFactory sagaFactory) {
    return new SimpleSagaBus(sagaFactory);
  }

  @Bean
  public SagaRepository repository() {
    return new InMemorySagaRepository();
  }

  @Bean
  public SagaFactory sagaFactory(SagaRepository sagaRepository, CommandBus commandBus) {
    return new SagaFactory(sagaRepository, commandBus);
  }

  @Bean
  public SagaStreamManager sagaStreamManager(StreamSubscriptionManager streamSubscriptionManager, SagaRepository sagaRepository, CommandBus commandBus, SagaInterestMatcher sagaInterestMatcher, SagaFactory sagaFactory, SagaLoader sagaLoader) {
    return new SagaStreamManager(streamSubscriptionManager, sagaRepository, commandBus, sagaInterestMatcher, sagaFactory, sagaLoader);
  }

  @Bean
  public StreamSubscriptionManager subscriptionManager(NewtonEventClient eventClient, EventStreamIndexStore eventStreamIndexStore, LockService lockService, EventStreamProcessor eventStreamProcessor) {
    return new MuonClusterAwareTrackingSubscriptionManager(eventClient, eventStreamIndexStore, lockService, eventStreamProcessor);
  }

  @Bean
  public SagaInterestMatcher sagaInterestMatcher() {
    return new SagaInterestMatcher();
  }

  @Scope("prototype")
  @Bean
  public SagaEndCommand sagaEndCommand() {
    return new SagaEndCommand();
  }
}
