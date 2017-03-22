package io.muoncore.newton.eventsource.muon;


import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.OnDomainEvent;
import io.muoncore.newton.UUIDIdentifier;

public class TestAggregate extends AggregateRoot<UUIDIdentifier> {

	public TestAggregate() {
	}

	public TestAggregate(UUIDIdentifier id) {
		raiseEvent(new TestAggregateCreated(id));
	}

	@OnDomainEvent
	public void handle(TestAggregateCreated created) {
		this.id = created.getId();
	}
}
