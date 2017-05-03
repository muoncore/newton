package io.muoncore.newton.eventsource.muon;


import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.OnDomainEvent;
import io.muoncore.newton.SimpleAggregateRootId;
import io.muoncore.newton.eventsource.AggregateConfiguration;

@AggregateConfiguration(context = "${spring.application.name}")
public class TestAggregate extends AggregateRoot<SimpleAggregateRootId> {

	public TestAggregate() {
	}

	public TestAggregate(SimpleAggregateRootId id) {
		raiseEvent(new TestAggregateCreated(id));
	}

	@OnDomainEvent
	public void handle(TestAggregateCreated created) {
		this.id = created.getId();
	}
}
