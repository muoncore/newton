package io.muoncore.newton;

import io.muoncore.newton.command.CommandConfiguration;
import io.muoncore.newton.eventsource.muon.MuonEventSourceConfiguration;
import io.muoncore.newton.query.QueryConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {
	CommandConfiguration.class,
	MuonEventSourceConfiguration.class,
	QueryConfiguration.class
})
public class MicroServiceStarterAutoConfiguration {

}
