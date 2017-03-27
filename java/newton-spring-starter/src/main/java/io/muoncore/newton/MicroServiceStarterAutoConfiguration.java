package io.muoncore.newton;

import io.muoncore.newton.command.CommandConfiguration;
import io.muoncore.newton.eventsource.muon.MuonEventSourceConfiguration;
import io.muoncore.newton.mongo.MongoConfiguration;
import io.muoncore.newton.query.QueryConfiguration;
import io.muoncore.newton.saga.SagaConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {
	CommandConfiguration.class,
	MuonEventSourceConfiguration.class,
	QueryConfiguration.class,
  SagaConfiguration.class,
  MongoConfiguration.class
})
public class MicroServiceStarterAutoConfiguration {

}
