package io.muoncore.newton;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AggregateRootTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void raiseEvent() throws Exception {
		//GIVEN
		TestAggregateRoot testAggregate = new TestAggregateRoot();
		//WHEN
		testAggregate.doA("ZZZ");
		//THEN
		assertEquals("A event raised and handled", "ZZZ", testAggregate.getAString());
		assertTrue("New Operations contains AEvent", testAggregate.getNewOperations().contains(new AEvent("ZZZ")));
	}

	@Test
	public void raiseEvent_HandlerNotImplemented() throws Exception {
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage(Matchers.startsWith("Undefined domain event handler method"));
		//GIVEN
		TestAggregateRoot testAggregate = new TestAggregateRoot();
		//WHEN
		testAggregate.doB("ZZZ");
	}


	@Data
	public class TestAggregateRoot extends AggregateRoot {

		private String aString;

		void doA(String a) {
			raiseEvent(new AEvent(a));
		}

		void doB(String b) {
			raiseEvent(new BEvent());
		}

		@OnDomainEvent
		public void handle(AEvent aEvent) {
			this.aString = aEvent.getValue();
		}

	}

	@Data
	@AllArgsConstructor
	public class AEvent implements NewtonEvent {

		private String value;
	}

	public class BEvent implements NewtonEvent {

	}


}
