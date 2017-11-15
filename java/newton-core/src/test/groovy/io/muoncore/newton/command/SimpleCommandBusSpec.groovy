package io.muoncore.newton.command

import io.muoncore.newton.NewtonEvent
import spock.lang.Specification

class SimpleCommandBusSpec extends Specification {

  def "async api works the same as sync"() {
    def cmd = Mock(Command) {
      executeAndReturnEvents() >> [Mock(NewtonEvent)]
    }
    CommandFactory factory = Mock(CommandFactory)
    def bus = new SimpleCommandBus(factory)
    def events

    when:
    bus.dispatchAsync(CommandIntent.builder(cmd.class.name).build()).get().onSuccess {
      events = it
    }

    then:
    1 * factory.create(_, _, _, _, _) >> cmd
    events.size() == 1
  }

  def "returns event list when called"() {
    def cmd = Mock(Command) {
      executeAndReturnEvents() >> [Mock(NewtonEvent)]
    }
    CommandFactory factory = Mock(CommandFactory)
    def bus = new SimpleCommandBus(factory)
    def events

    when:
    bus.dispatch(CommandIntent.builder(cmd.class.name).build()).get().onSuccess {
      events = it
    }

    then:
    1 * factory.create(_, _, _, _, _) >> cmd
    events.size() == 1
  }

  def "generates error event when event throws exception"() {
    def cmd = Mock(Command) {
      executeAndReturnEvents() >> { throw new IllegalArgumentException("Broke!")}
    }
    CommandFactory factory = Mock(CommandFactory)
    def bus = new SimpleCommandBus(factory)

    CommandFailedEvent errorEvent

    when:
    bus.dispatch(CommandIntent.builder(cmd.class.name).build()).get().onError {
      errorEvent = it
    }

    then:
    1 * factory.create(_, _, _, _, _) >> cmd
    errorEvent
    errorEvent.failureMessage == "Broke!"
  }
}


class FakedCommand implements Command {
  @Override
  void execute() {

  }
}
