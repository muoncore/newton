package todo

import io.muoncore.Muon
import io.muoncore.MuonBuilder
import io.muoncore.config.AutoConfiguration
import io.muoncore.config.MuonConfigBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
class MuonTestConfig {

    @Bean
    Muon muon() {
        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("taskservice").build()

      MuonBuilder.withConfig(config).build()
    }

}
