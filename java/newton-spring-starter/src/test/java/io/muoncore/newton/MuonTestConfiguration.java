package io.muoncore.newton;

import com.google.common.eventbus.EventBus;
import io.muoncore.MultiTransportMuon;
import io.muoncore.Muon;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.eventstore.TestEventStore;
import io.muoncore.memory.discovery.InMemDiscovery;
import io.muoncore.memory.transport.InMemTransport;
import io.muoncore.newton.cluster.LockService;
import io.muoncore.newton.saga.SagaLoader;
import io.muoncore.protocol.event.client.AggregateEventClient;
import io.muoncore.protocol.event.client.DefaultEventClient;
import io.muoncore.protocol.event.client.EventClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

@Profile("test")
@Configuration
public class MuonTestConfiguration {

	@Autowired
	//Don't remove as it's required for tests - Spring lazy-loads beans & thus causes tests to fail as event store cannot be found!!!
	private TestEventStore testEventStore;

	@Bean
	public InMemDiscovery discovery() {
		return new InMemDiscovery();
	}

	@Bean
	public EventBus bus() {
		return new EventBus();
	}

	@Bean
	public Muon muon() {
		AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("test-service")
			.build();

		return new MultiTransportMuon(config, discovery(),
			Collections.singletonList(
				new InMemTransport(config, bus())
			),
			new JsonOnlyCodecs());
	}

	@Bean
	public TestEventStore testEventStore() throws InterruptedException {
		AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("photon-mini")
			.withTags("eventstore")
			.build();
		//Another separate instance of muon is fired up to ensure....
		Muon muon = new MultiTransportMuon(config, discovery(),
			Collections.singletonList(new InMemTransport(config, bus())),
			new JsonOnlyCodecs());

		return new TestEventStore(muon);
	}
	@Bean
	public EventClient eventClient(Muon muon) {
		return new DefaultEventClient(muon);
	}

	@Bean
	public AggregateEventClient aggregateEventClient(EventClient eventClient) {
		return new AggregateEventClient(eventClient);
	}

	@Bean
	public LockService lockService() throws Exception {
		return (name, exec) -> exec.execute((LockService.TaskLockControl) () -> {});
	}

	@Bean
	public SagaLoader sagaLoader() {
		return interest -> (Class) MuonTestConfiguration.class.getClassLoader().loadClass(interest.getSagaClassName());
	}
}
