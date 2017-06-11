package io.muoncore.newton;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
public abstract class InMemoryUniqueAggregateDomainService<V> implements UniqueAggregateDomainService<V> {

  //TODO, this is now object to value. Possibly this should be generified to match the ID we are interested in ...
  private Map<Object, V> entriesMap = Collections.synchronizedMap(new HashMap<>());

	private StreamSubscriptionManager streamSubscriptionManager;
	private DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, EventHandler.class);

	public InMemoryUniqueAggregateDomainService(StreamSubscriptionManager streamSubscriptionManager) throws IOException {
		this.streamSubscriptionManager = streamSubscriptionManager;
	}

	private void handleEvent(NewtonEvent event) {
	  log.debug("Accepting event for view " + getClass() + ": " + event);
		eventAdaptor.accept(event);
	}

	@PostConstruct
	public void initSubscription() throws InterruptedException {
    Arrays.stream(eventStreams()).forEach(stream-> streamSubscriptionManager.localNonTrackingSubscription(stream, this::handleEvent));
	}

  protected abstract String[] eventStreams();

  @Override
  public boolean isUnique(Object thisId, V value) {
		return !exists(thisId, value);
	}

  @Override
  public boolean exists(V value) {
	  return exists(null, value);
  }

  @Override
  public boolean exists(Object thisId, V value) {
		if (thisId != null) {
			return entriesMap.entrySet().stream().anyMatch(x -> x.getValue().equals(value) && !x.getKey().equals(thisId));
		}
		return entriesMap.values().stream().anyMatch(v -> v.equals(value));
	}

	@Override
  public void addValue(Object id, V value) {
		entriesMap.put(id, value);
	}

	@Override
  public void removeValue(Object id) {
		entriesMap.remove(id);
	}

	@Override
  public void updateValue(Object id, V value) {
		entriesMap.entrySet().stream()
			.filter(entry -> entry.getKey().equals(id))
			.findFirst()
			.ifPresent(entry -> entry.setValue(value));
	}

	protected Optional<V> find(Predicate<V> predicate) {
		return entriesMap.values().stream().filter(predicate).findFirst();
	}


}
