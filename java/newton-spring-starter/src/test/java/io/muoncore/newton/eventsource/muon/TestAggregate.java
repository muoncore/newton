package io.muoncore.newton.eventsource.muon;


import io.muoncore.newton.AggregateRoot;
import io.muoncore.newton.OnDomainEvent;
import io.muoncore.newton.DocumentId;

public class TestAggregate extends AggregateRoot<DocumentId> {

	public TestAggregate() {
	}

	public TestAggregate(DocumentId id) {
		raiseEvent(new TestAggregateCreated(id));
	}

	@OnDomainEvent
	public void handle(TestAggregateCreated created) {
		this.id = created.getId();
	}
}
