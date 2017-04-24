package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import static org.junit.Assert.*;

public class SagaInterestMatcherTest {
    @Test
    public void matchesSuccess() throws Exception {
        SagaInterestMatcher matcher = new SagaInterestMatcher();

        assertTrue(matcher.matches(new TestEvent("hello world"), new SagaInterest(
                TestSaga.class.getName(), TestEvent.class.getCanonicalName(), new DocumentId(), new DocumentId(), "myId", "hello world")));

    }

    @Test
    public void matchesFailure() throws Exception {

    }

    @Data
    @AllArgsConstructor
    static class TestEvent implements NewtonEvent {
        private String myId;
        private final DocumentId id = new DocumentId();
    }
}
