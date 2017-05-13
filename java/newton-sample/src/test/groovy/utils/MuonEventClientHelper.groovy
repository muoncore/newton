package utils

import io.muoncore.Muon
import io.muoncore.MuonBuilder
import io.muoncore.config.AutoConfiguration
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.protocol.event.client.DefaultEventClient
import io.muoncore.protocol.event.client.EventClient

class MuonEventClientHelper {

	static EventClient create(String serviceId) {
		return create(serviceId, "amqp://guest:guest@localhost")
	}

	static EventClient create(String serviceId, String amqpUrl) {
		AutoConfiguration config = MuonConfigBuilder
			.withServiceIdentifier(serviceId)
			.addWriter(
			{ AutoConfiguration autoConfiguration ->
				autoConfiguration.getProperties().put("amqp.transport.url", amqpUrl);
				autoConfiguration.getProperties().put("amqp.discovery.url", amqpUrl);
			})
			.build()

		Muon muon = MuonBuilder.withConfig(config).build()
		muon.getDiscovery().blockUntilReady()

		EventClient client = new DefaultEventClient(muon)

		return client
	}

}
