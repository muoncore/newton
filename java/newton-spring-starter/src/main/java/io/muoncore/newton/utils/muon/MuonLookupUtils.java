package io.muoncore.newton.utils.muon;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.query.RebuildingStreamView;
import io.muoncore.newton.saga.Saga;
import io.muoncore.newton.AggregateRoot;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class MuonLookupUtils {

	private static Map<String, Class<? extends Saga>> sagaTypeMappings;
	private static Map<String, Class<? extends NewtonEvent>> eventTypeMappings;
	private static Map<String, Class<? extends AggregateRoot>> aggregateRootMappings;

	private static Map<String, Class> views;

	static CountDownLatch ready = new CountDownLatch(1);

	static void init(String[] packages) {

    List<URL> urls = new ArrayList<>();
    urls.addAll(ClasspathHelper.forPackage("io.muoncore.newton", MuonLookupUtils.class.getClassLoader()));
    for (String aPackage : packages) {
      log.info("Adding package {}", aPackage);

      Collection<URL> urls1 = ClasspathHelper.forPackage(aPackage, MuonLookupUtils.class.getClassLoader());

      log.info("Got {}", urls1);
      urls.addAll(urls1);
    }

    log.info("Booting Reflections with urls {}", urls);

		Reflections reflections = new Reflections(new ConfigurationBuilder()
      .addScanners(new SubTypesScanner())
      .addScanners(new TypeAnnotationsScanner())
      .setUrls(urls));

    final Set<Class<?>> rebuildingViewTypes = reflections.getTypesAnnotatedWith(RebuildingStreamView.class);
    views = new HashMap<>();
    for (Class<?> cibecsEvent : rebuildingViewTypes) {
      views.put(cibecsEvent.getSimpleName(), cibecsEvent);
    }

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
		ready.countDown();
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

  public static Collection<Class<?extends AggregateRoot>> listAllAggregateRootClass() {
    waitForMappingsToBeInitialized();
    return aggregateRootMappings.values();
  }

	private static void waitForMappingsToBeInitialized() {
    try {
      ready.await();
    } catch (InterruptedException e) {
      //NOTHING
    }
	}
}
