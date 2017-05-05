package io.muoncore.newton;

import io.muoncore.newton.command.CommandConfiguration;
import io.muoncore.newton.eventsource.muon.MuonEventSourceConfiguration;
import io.muoncore.newton.mongo.MongoConfiguration;
import io.muoncore.newton.query.QueryConfiguration;
import io.muoncore.newton.saga.SagaConfiguration;
import io.muoncore.newton.utils.muon.EnableNewtonRegistrar;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {
	MuonEventSourceConfiguration.class,
	CommandConfiguration.class,
	QueryConfiguration.class,
  SagaConfiguration.class,
  MongoConfiguration.class
})
@AutoConfigureAfter(EnableNewtonRegistrar.class)
public class MicroServiceStarterAutoConfiguration {

}
