package io.muoncore.newton.eventsource.muon;


import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.EventHandler;
import io.muoncore.newton.eventsource.AggregateConfiguration;
import lombok.Getter;

@AggregateConfiguration(context = "${spring.application.name}")
public class TestAggregate extends AggregateRoot<String> {

  @Getter
  String id;

	public TestAggregate() {
	}

	public TestAggregate(String id) {
		raiseEvent(new TestAggregateCreated(id));
	}

	@EventHandler
	public void handle(TestAggregateCreated created) {
		this.id = created.getId();
	}
}
