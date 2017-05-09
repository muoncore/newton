package io.muoncore.newton;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
public abstract class UniqueAggregateDomainService<V> {

  @Value("${spring.application.name}")
  private String appName;

  private Map<AggregateRootId, V> entriesMap = Collections.synchronizedMap(new HashMap<>());

	private StreamSubscriptionManager streamSubscriptionManager;
	private Class<? extends AggregateRoot> aggregateType;
	private DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, EventHandler.class);

	public UniqueAggregateDomainService(StreamSubscriptionManager streamSubscriptionManager, Class<? extends AggregateRoot> aggregateType) throws IOException {
		this.streamSubscriptionManager = streamSubscriptionManager;
		this.aggregateType = aggregateType;
	}

	private void handleEvent(NewtonEvent event) {
	  log.debug("Accepting event for view " + getClass() + ": " + event);
		eventAdaptor.accept(event);
	}

	@PostConstruct
	public void initSubscription() throws InterruptedException {
		//todo: investigate AggregateRootContext annotation....

		String streamName = aggregateType.getSimpleName();
		if (this.appName != null){
			streamName = this.appName.concat("/").concat(streamName);
		}
		streamSubscriptionManager.localNonTrackingSubscription(streamName, this::handleEvent);
	}

	public boolean isUnique(AggregateRootId thisId, V value) {
		return !exists(thisId, value);
	}

  public boolean exists(V value) {
	  return exists(null, value);
  }

  public boolean exists(AggregateRootId thisId, V value) {
		if (thisId != null) {
			return entriesMap.entrySet().stream().anyMatch(x -> x.getValue().equals(value) && !x.getKey().equals(thisId));
		}
		return entriesMap.values().stream().anyMatch(v -> v.equals(value));
	}

	public void addValue(AggregateRootId id, V value) {
		entriesMap.put(id, value);
	}

	public void removeValue(AggregateRootId id) {
		entriesMap.remove(id);
	}

	public void updateValue(AggregateRootId id, V value) {
		entriesMap.entrySet().stream()
			.filter(entry -> entry.getKey().equals(id))
			.findFirst()
			.ifPresent(entry -> entry.setValue(value));
	}

	protected Optional<V> find(Predicate<V> predicate) {
		return entriesMap.values().stream().filter(predicate).findFirst();
	}


}
