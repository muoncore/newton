package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.command.CommandBus;
import io.muoncore.newton.eventsource.AggregateRootUtil;
import io.muoncore.newton.eventsource.muon.MuonEventSourceRepository;
import io.muoncore.newton.saga.events.SagaLifecycleEvent;
import io.muoncore.newton.utils.muon.MuonLookupUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Sagas, aka Process Managers are long running transactional components in Newton. They can be through of as state
 * machines that consume events from other parts of the system and produce Commands to mutate it further.
 *
 * `SagaStreamManager` manages the lifecycle of Saga instances.  App code instances of a Saga are discovered at boot time
 * by this class and then instantiated from the spring app context as necessary whenever an event arrives that a particular
 * Saga is interested in. This interest comes in the form of @{@link StartSagaWith} annotations to describe how a process should begin,
 * or via {@link SagaInterest} registrations that describe subsequent steps in the workflow.
 *
 * @see SagaInterest
 * @see StartSagaWith
 */
@Slf4j
public class SagaStreamManager {

  public final static String SAGA_LIFECYCLE_STREAM = "saga-lifecycle";

  private SagaRepository sagaRepository;
  private StreamSubscriptionManager streamSubscriptionManager;
  private CommandBus commandBus;
  private SagaInterestMatcher sagaInterestMatcher;
  private Set<String> subscribedStreams = new HashSet<>();
  private SagaFactory sagaFactory;
  private SagaLoader sagaLoader;
  //avoid potential deadlock by doing all work on a different thread, not the event dispatch thread.
  private Executor worker = Executors.newCachedThreadPool();

  private SagaStartCache sagaStartCache = new SagaStartCache();

  public SagaStreamManager(
    StreamSubscriptionManager streamSubscriptionManager,
    SagaRepository sagaRepository,
    CommandBus commandBus, SagaInterestMatcher sagaInterestMatcher, SagaFactory sagaFactory, SagaLoader sagaLoader) {
    this.streamSubscriptionManager = streamSubscriptionManager;
    this.sagaRepository = sagaRepository;
    this.commandBus = commandBus;
    this.sagaInterestMatcher = sagaInterestMatcher;
    this.sagaFactory = sagaFactory;
    this.sagaLoader = sagaLoader;
  }

  @EventListener
  public void onApplicationEvent(ApplicationReadyEvent onReadyEvent) {
    listenToLifecycleEvents();

    worker.execute(() -> {
      MuonLookupUtils.listAllSagas().forEach(aClass -> {
        try {
          if (Modifier.isInterface(aClass.getModifiers()) ||
            Modifier.isAbstract(aClass.getModifiers())) return;
          processSaga(aClass);
        } catch (IllegalStateException e) {
          log.error("Unable to initialise saga " + aClass, e);
        }
      });
    });
  }

  public void listenToLifecycleEvents() {
    streamSubscriptionManager.localTrackingSubscription("SAGA_LIFECYCLE_STREAM", SAGA_LIFECYCLE_STREAM, event -> {
      SagaLifecycleEvent lifecycleEvent = (SagaLifecycleEvent) event;
      sagaFactory.notifySagaLifeCycle(lifecycleEvent.getId(), lifecycleEvent);
    });
  }

  public void processSaga(Class<? extends Saga> saga) {

    SagaStreamConfig[] s = saga.getAnnotationsByType(SagaStreamConfig.class);

    if (s.length == 0) throw new IllegalStateException("Saga does not have @SagaStreamConfig: " + saga);

    recordSagaCreatedByEvent(saga);

    List<String> streams = new ArrayList<>();
    streams.addAll(Arrays.asList(s[0].streams()));

    Arrays.asList(s[0].aggregateRoots()).forEach(aggregateRootClass ->
      streams.add(AggregateRootUtil.getAggregateRootStream(aggregateRootClass))
    );

    for (String stream : streams) {
      if (!subscribedStreams.contains(stream)) {
        subscribedStreams.add(stream);
        streamSubscriptionManager.globallyUniqueSubscriptionFromNow("saga-manager-" + stream, stream, event -> {
          worker.execute(() -> {
            processEvent(event);
          });
        });
      }
    }
  }

  private void recordSagaCreatedByEvent(Class<? extends Saga> saga) {
    final Method[] methods = saga.getMethods();
    for (Method method : methods) {
      if (method.getName().startsWith("lambda$") || !method.isAnnotationPresent(StartSagaWith.class)) {
        continue;
      }
      final Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length == 1) {
        Class newtonEv = parameterTypes[0];
        sagaStartCache.add(newtonEv, saga);
        return;
      }
    }

    log.error(String.format("Saga type %s does not have @StartSagaWith. This is an error, and this Saga cannot be started via an event ", saga.getName()));
//    throw new IllegalStateException(String.format("Saga type %s does not have @StartSagaWith. This is an error, and this Saga cannot be started via an event ", saga.getName()));
  }

  public void processEvent(NewtonEvent event) {
    log.debug("Checking if there is a Saga to be run for " + event.getClass());

    startSagasFor(event);
    extractSagasForEventAndHandle(event);
  }

  private void extractSagasForEventAndHandle(NewtonEvent event) {
    List<SagaInterest> interests = sagaRepository.getSagasInterestedIn(event.getClass());

    interests.forEach(interest -> {
      try {
        if (!sagaInterestMatcher.matches(event, interest)) {
          return;
        }
        Optional<? extends Saga> saga = sagaRepository.load(interest.getSagaId(), sagaLoader.loadSagaClass(interest));
        saga.ifPresent(saga1 -> {
          sagaFactory.autowire(saga1);
          worker.execute(() -> {
            MuonEventSourceRepository.executeCausedBy(event, () -> {
              saga1.handle(event);
              sagaRepository.save(saga1);
              sagaFactory.processCommands(saga1);
              sagaRepository.save(saga1);
              return null;
            });
          });
        });

      } catch (ClassNotFoundException e) {
        throw new RuntimeException("The given Saga type " + interest.getClassName() + " does not exist on the classpath", e);
      }
    });
  }

  private void startSagasFor(NewtonEvent event) {
    Set<Class<? extends Saga>> sagas = sagaStartCache.find(event.getClass());

    if (sagas.size() > 0){
      log.debug("Will starts Sagas {}", sagas);
    }

    sagas.forEach(sagaClass -> sagaFactory.create(sagaClass, event));
  }
}
