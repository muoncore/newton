package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Test;

import static org.junit.Assert.*;

public class SagaInterestMatcherTest {
    @Test
    public void matchesSuccess() throws Exception {
        SagaInterestMatcher matcher = new SagaInterestMatcher();

        assertTrue(matcher.matches(new TestEvent("hello world"), new SagaInterest(
                SagaIntegrationTests.TestSaga.class.getName(), TestEvent.class.getCanonicalName(), new DocumentId(), new DocumentId(), "myId", "hello world")));

    }

    @Test
    public void matchesFailure() throws Exception {

    }

    @AllArgsConstructor
    static class TestEvent implements NewtonEvent {
        @Getter
        private String myId;
    }
}
