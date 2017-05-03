package io.muoncore.newton.saga;

import io.muoncore.newton.AggregateRootId;
import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.SimpleAggregateRootId;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SagaInterestMatcherTest {
    @Test
    public void matchesSuccess() throws Exception {
        SagaInterestMatcher matcher = new SagaInterestMatcher();

        assertTrue(matcher.matches(new TestEvent("hello world"), new SagaInterest(
                TestSaga.class.getName(), TestEvent.class.getCanonicalName(), new SimpleAggregateRootId(), new SimpleAggregateRootId(), "myId", "hello world")));

    }

    @Test
    public void matchesFailure() throws Exception {

    }

    @Data
    @AllArgsConstructor
    static class TestEvent implements NewtonEvent {
        private String myId;
        private final AggregateRootId id = new SimpleAggregateRootId();
    }
}
