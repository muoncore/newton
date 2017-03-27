package io.muoncore.newton;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
public abstract class UniqueAggregateDomainService<V> {

	private Map<DocumentId, V> entriesMap = Collections.synchronizedMap(new HashMap<>());

	private StreamSubscriptionManager streamSubscriptionManager;
	private Class<? extends AggregateRoot> aggregateType;
	private DynamicInvokeEventAdaptor eventAdaptor = new DynamicInvokeEventAdaptor(this, OnViewEvent.class);

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
		streamSubscriptionManager.localNonTrackingSubscription(aggregateType.getSimpleName(), this::handleEvent);
	}

	public boolean isUnique(DocumentId thisId, V value) {
		return !exists(thisId, value);
	}

	public boolean exists(DocumentId thisId, V value) {
		if (thisId != null) {
			return entriesMap.entrySet().stream().anyMatch(x -> x.getValue().equals(value) && !x.getKey().equals(thisId));
		}
		return entriesMap.values().stream().anyMatch(v -> v.equals(value));
	}

	public void addValue(DocumentId id, V value) {
		entriesMap.put(id, value);
	}

	public void removeValue(DocumentId id) {
		entriesMap.remove(id);
	}

	public void updateValue(DocumentId id, V value) {
		entriesMap.entrySet().stream()
			.filter(entry -> entry.getKey().equals(id))
			.findFirst()
			.ifPresent(entry -> entry.setValue(value));
	}

	protected Optional<V> find(Predicate<V> predicate) {
		return entriesMap.values().stream().filter(predicate).findFirst();
	}


}
