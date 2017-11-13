package io.muoncore.newton.saga;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.muoncore.api.MuonFuture;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.command.CommandBus;
import io.muoncore.newton.command.CommandResult;
import io.muoncore.newton.eventsource.muon.MuonEventSourceRepository;
import io.muoncore.newton.saga.events.SagaLifecycleEvent;
import lombok.extern.slf4j.Slf4j;
import io.muoncore.newton.command.CommandIntent;
import io.muoncore.newton.saga.events.SagaEndEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
public class SagaFactory implements ApplicationContextAware {

  private ApplicationContext applicationContext;
  private SagaRepository sagaRepository;
  private CommandBus commandBus;

  private EventBus bus = new EventBus();

  public SagaFactory(SagaRepository sagaRepository, CommandBus commandBus) {
    this.commandBus = commandBus;
    this.sagaRepository = sagaRepository;
  }

  public void autowire(Saga saga) {
    applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(saga, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
  }

  public void notifySagaLifeCycle(Object id, SagaLifecycleEvent event) {
    bus.post(event);
  }

  @SuppressWarnings("unchecked")
  public <T extends Saga> SagaMonitor<T> create(Class<T> sagaType, NewtonEvent payload) {
    log.debug("Creating new saga of type " + sagaType + " with payload " + payload);
    T saga = (T) loadFromSpringContext(sagaType);
    final T thesaga = saga;

    return MuonEventSourceRepository.executeCausedBy(payload, () -> {
      thesaga.startWith(payload);

      sagaRepository.saveNewSaga(saga, payload);

      EventedSagaMonitor monitor = new EventedSagaMonitor(saga.getId(), sagaType);

      processCommands(saga);

      sagaRepository.save(saga);

      return monitor;
    });

  }

  public <T extends Saga> SagaMonitor<T> monitor(String sagaId, Class<T> type) {
    Optional<T> saga = sagaRepository.load(sagaId, type);
    if (!saga.isPresent()) {
      throw new IllegalStateException("Saga with ID " + sagaId + " does not exist");
    }
    return new EventedSagaMonitor(sagaId, type);
  }

  void processCommands(Saga saga) {
    List<CommandIntent> newOperations = new ArrayList<>(saga.getNewOperations());
    saga.getNewOperations().clear();
    for (CommandIntent intent : newOperations) {
      MuonFuture<CommandResult> dispatch = commandBus.dispatch(intent);
      try {
        CommandResult commandResult = dispatch.get();
        commandResult.getFailure().ifPresent(event -> {
          MuonEventSourceRepository.executeCausedBy(event, () -> {
            saga.handle(event);
            processCommands(saga);
            return null;
          });
        });
      } catch (InterruptedException | ExecutionException e) {
        log.warn("Error extracting the command result for a saga", e);
      }
    }
  }


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  private Saga loadFromSpringContext(Class<? extends Saga> sagaType) {
    return applicationContext.getBean(sagaType);
  }

  class EventedSagaMonitor<T extends Saga> implements SagaMonitor<T> {
    private String id;
    private Class<T> sagaType;
    private List<SagaListener> listeners = new ArrayList<>();
    boolean finished;
    private BlockingQueue<SagaLifecycleEvent> events = new LinkedBlockingQueue<>();

    public EventedSagaMonitor(String id, Class<T> sagaType) {
      this.id = id;
      this.sagaType = sagaType;
      bus.register(this);
    }

    @Override
    public String getId() {
      return id;
    }

    private void dispatchListener(SagaListener listener, SagaLifecycleEvent event) {
      if (event instanceof SagaEndEvent) {
        finished = true;
        log.debug("Received SagaEndEvent, and have local ID, releasing listeners and removing local monitor");
        T saga = sagaRepository.load(getId(), sagaType).get();
        listener.onComplete(saga);
        bus.unregister(this);
      }
    }

    public void dispatchListeners(SagaLifecycleEvent event) {
      events.add(event);

      listeners.stream().forEach(listener -> dispatchListener(listener, event));
    }

    @Override
    public void onFinished(SagaListener eventListener) {
      synchronized (listeners) {
        listeners.add(eventListener);

        events.stream().forEach(event -> {
          dispatchListener(eventListener, event);
        });
      }
    }

    @Subscribe
    public void handle(SagaLifecycleEvent event) {
      if (event.getId().equals(getId())) {
        dispatchListeners(event);
      }
    }

    @Override
    public T waitForCompletion(TimeUnit unit, long timeout) {
      if (!finished) {
        CountDownLatch latch = new CountDownLatch(1);
        onFinished(saga -> {
          latch.countDown();
        });

        try {
          latch.await(timeout, unit);
        } catch (InterruptedException e) {
        }
      }
      return sagaRepository.load(id, sagaType).get();
    }
  }
}
