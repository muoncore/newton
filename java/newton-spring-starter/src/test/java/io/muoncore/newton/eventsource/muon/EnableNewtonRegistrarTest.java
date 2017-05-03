package io.muoncore.newton.eventsource.muon;

import io.muoncore.newton.EnableNewton;
import io.muoncore.newton.MuonTestConfiguration;
import io.muoncore.newton.SimpleAggregateRootId;
import io.muoncore.newton.eventsource.EventSourceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@ActiveProfiles({"test", "log-events"})
@Import({MuonTestConfiguration.class})
@RunWith(SpringRunner.class)
@Configuration
@EnableNewton
public class EnableNewtonRegistrarTest {

  @Autowired
  EventSourceRepository<TestAggregate> repo;

  @Test
  public void testAutoCreatedRepo() {
    assertNotNull(repo);
    SimpleAggregateRootId id = new SimpleAggregateRootId();
    repo.newInstance(() -> new TestAggregate(id));

    assertNotNull(repo.load(id));
  }
}
