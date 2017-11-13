package io.muoncore.newton.saga;

import io.muoncore.newton.NewtonEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SagaInterestMatcherTest {
  @Test
  public void matchesSuccess() throws Exception {
    SagaInterestMatcher matcher = new SagaInterestMatcher();

    // Given a test event and an interest registered in the Saga
    final TestEvent testEvent = new TestEvent("hello world");

    final SagaInterest sagaInterest = new SagaInterest(
      TestSaga.class.getName(),
      TestEvent.class.getCanonicalName(),
      "saga-id",
      "some-id",
      "myId",
      "hello world"
    );

    // When match is attempted
    final boolean result = matcher.matches(testEvent, sagaInterest);

    // The the match is found
    assertTrue(result);
  }

  @Test
  public void matchesFailure() throws Exception {

  }

  @Data
  @AllArgsConstructor
  static class TestEvent implements NewtonEvent {
    private String myId;
    private final String id = "hello-world";
  }
}
