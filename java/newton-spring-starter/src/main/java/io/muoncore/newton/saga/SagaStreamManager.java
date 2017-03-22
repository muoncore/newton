package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.command.CommandBus;
import lombok.extern.slf4j.Slf4j;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.command.CommandIntent;
import io.muoncore.newton.saga.events.SagaLifecycleEvent;
import io.muoncore.newton.utils.muon.MuonLookupUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    private Executor worker = Executors.newSingleThreadExecutor();

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
        MuonLookupUtils.listAllSagas().forEach(aClass -> {
            try {
                if (Modifier.isInterface(aClass.getModifiers()) ||
                        Modifier.isAbstract(aClass.getModifiers())) return;

                processSaga(aClass);
            } catch (IllegalStateException e) {
                log.error("Unable to initialise saga " + aClass, e);
            }
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

        String[] streams = s[0].streams();

        for(String stream: streams) {
            if (!subscribedStreams.contains(stream)) {
                subscribedStreams.add(stream);
                streamSubscriptionManager.globallyUniqueSubscription("saga-manager-" + stream, stream, event -> {
                    worker.execute(() -> {
                        processEvent(event);
                    });
                });
            }
        }
    }

    public void processEvent(NewtonEvent event) {

        List<SagaInterest> interests = sagaRepository.getSagasInterestedIn(event.getClass());

        interests.forEach(interest -> {
            try {
                if (!sagaInterestMatcher.matches(event, interest)) {
                    return;
                }
                Optional<? extends Saga> saga = sagaRepository.load(interest.getId(), sagaLoader.loadSagaClass(interest));
                saga.ifPresent(saga1 -> {
                    saga1.handle(event);
                    sagaRepository.save(saga1);
                    processCommands(saga1);
                });

            } catch (ClassNotFoundException e) {
                throw new RuntimeException("The given Saga type " + interest.getClassName() + " does not exist on the classpath", e);
            }
        });
    }

    private void processCommands(Saga saga) {
        for (CommandIntent intent : (List<CommandIntent>)saga.getNewOperations()) {
            commandBus.dispatch(intent);
        }
        saga.getNewOperations().clear();
    }
}
