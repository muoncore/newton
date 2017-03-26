package io.muoncore.newton.utils.muon;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.saga.Saga;
import io.muoncore.newton.AggregateRoot;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MuonLookupUtils {

	private static Map<String, Class<? extends Saga>> sagaTypeMappings;
	private static Map<String, Class<? extends NewtonEvent>> eventTypeMappings;
	private static Map<String, Class<? extends AggregateRoot>> aggregateRootMappings;

	static {
		Reflections reflections = new Reflections("mytown", new SubTypesScanner());
		final Set<Class<? extends NewtonEvent>> eventTypes = reflections.getSubTypesOf(NewtonEvent.class);
		eventTypeMappings = new HashMap<>();
		for (Class<? extends NewtonEvent> cibecsEvent : eventTypes) {
			eventTypeMappings.put(cibecsEvent.getSimpleName(), cibecsEvent);
		}

		final Set<Class<? extends AggregateRoot>> aggregateRootTypes = reflections.getSubTypesOf(AggregateRoot.class);
		aggregateRootMappings = new HashMap<>();
		for (Class<? extends AggregateRoot> root : aggregateRootTypes) {
			aggregateRootMappings.put(root.getSimpleName(), root);
		}

		final Set<Class<? extends Saga>> sagaTypes = reflections.getSubTypesOf(Saga.class);
		sagaTypeMappings = new HashMap<>();
		for (Class<? extends Saga> root : sagaTypes) {
			sagaTypeMappings.put(root.getSimpleName(), root);
		}
	}

	public static Class<? extends NewtonEvent> getDomainClass(io.muoncore.protocol.event.Event event) {
		waitForMappingsToBeInitialized();
		return eventTypeMappings.get(event.getEventType());
	}

	public static Collection<Class<? extends Saga>> listAllSagas() {
		waitForMappingsToBeInitialized();
		return sagaTypeMappings.values();
	}

	public static Set<String> listAllEventTypes() {
		waitForMappingsToBeInitialized();
		return eventTypeMappings.keySet();
	}

	public static Set<String> listAllAggregateRoots() {
		waitForMappingsToBeInitialized();
		return aggregateRootMappings.keySet();
	}


	private static void waitForMappingsToBeInitialized() {
		try {
			while (eventTypeMappings == null || aggregateRootMappings == null) {
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			//DO NOTHING
		}
	}


}
