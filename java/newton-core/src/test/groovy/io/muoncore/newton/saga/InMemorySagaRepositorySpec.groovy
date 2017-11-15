package io.muoncore.newton.saga

import io.muoncore.newton.InMemoryTestConfiguration
import io.muoncore.newton.NewtonEvent
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@ActiveProfiles("test")
@SpringBootTest(classes = [InMemoryTestConfiguration, TestSagaConfiguration])
class InMemorySagaRepositorySpec extends Specification {

  @Autowired
  InMemorySagaRepository repository

  def "Can save an existing Saga"() {
    given:
    String sagaId = TestDataGenerators.aString()
    Saga aTestSaga = new SimpleSaga(id: sagaId)

    when:
    repository.save(aTestSaga)

    then:
    repository.load(sagaId, aTestSaga.class).get() == aTestSaga
    repository.getSagaStore().size() == 1
  }

  def "Saving a new Saga registers creation by the triggering Event"() {
    given:
    String sagaId = TestDataGenerators.aString()
    String eventId = TestDataGenerators.aString()
    Saga aTestSaga = new SimpleSaga(id: sagaId)
    NewtonEvent<String> event = new TriggerASimpleSagaEvent(id: eventId)

    when:
    repository.saveNewSaga(aTestSaga, event)

    then:
    def expectedSagaCreated = new SagaCreated(aTestSaga.getClass().name, eventId, sagaId)
    repository.getSagaCreatedStore().size() == 1
    repository.getSagaCreatedStore().get(eventId).size() == 1
    repository.getSagaCreatedStore().get(eventId).get(0) == expectedSagaCreated
  }

  private class SimpleSaga extends StatefulSaga {
  }

  private class TriggerASimpleSagaEvent implements NewtonEvent<String> {
    private String id = UUID.randomUUID().toString()

    @Override
    String getId() {
      return id
    }

    void setId(String id) {
      this.id = id
    }
  }

}

class TestDataGenerators {
  static String aString(Integer length = 10) {
    String charset = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()
    return RandomStringUtils.random(length, charset.toCharArray())
  }
}
