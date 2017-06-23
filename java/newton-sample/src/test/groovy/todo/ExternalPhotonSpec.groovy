package todo

import io.muoncore.newton.SampleApplication
import io.muoncore.newton.eventsource.muon.MuonEventSourceRepository
import io.muoncore.newton.support.DocumentId
import io.muoncore.newton.todo.Task
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@ActiveProfiles("test")
@SpringBootTest(classes = [MuonTestConfig, SampleApplication])
class MissingEventSpec extends Specification {

  @Autowired
  MuonEventSourceRepository<Task> repo

  def "when receives a non existent event type, is cast to EventTypeNotFound"() {

    Task task = repo.newInstance { new Task(new DocumentId(), "Hi!") }

    when: "emit an event with an unmappable type"
    20.times {

      def exec = {
        Task t = repo.load(task.id)
        t.changeDescription("Hello" + it)
        repo.save(t)
      }

      Thread.start exec
//      Thread.start exec
    }

    sleep(200)

    task = repo.load(task.id)

    then:
    task.description == "Hello19"
  }
}
