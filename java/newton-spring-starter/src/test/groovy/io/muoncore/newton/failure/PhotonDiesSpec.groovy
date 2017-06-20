package io.muoncore.newton.failure

import io.muoncore.eventstore.TestEventStore
import io.muoncore.newton.EnableNewton
import io.muoncore.newton.MuonTestConfiguration
import io.muoncore.newton.command.CommandConfiguration
import io.muoncore.newton.mongo.MongoConfiguration
import io.muoncore.newton.query.QueryConfiguration
import io.muoncore.newton.saga.SagaConfiguration
import io.muoncore.newton.saga.SagaIntegrationTests
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import spock.lang.Specification

@ActiveProfiles(["test", "log-events"])
@ContextConfiguration(classes = [QueryConfiguration.class, CommandConfiguration.class, MuonTestConfiguration.class, SagaIntegrationTests.class, SagaConfiguration.class, MongoConfiguration.class])
//@RunWith(SpringRunner.class)
@Configuration
@SpringBootTest
@EnableNewton("io.muoncore.newton.saga")
@ComponentScan
class PhotonDiesSpec extends Specification {

  @Autowired
  TestEventStore eventStore

  def "subscribes to both streams and aggregate roots"() {



    when:
    def wibble = "Hello"

    then:
    1==2
  }
}
