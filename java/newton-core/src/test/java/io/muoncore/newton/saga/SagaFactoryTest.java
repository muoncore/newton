package io.muoncore.newton.saga;

import io.muoncore.newton.MuonTestConfiguration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

//@Category(UnitIntegrationTest.class)
//@ActiveProfiles({"test"})
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {QueryConfiguration.class, CommandConfiguration.class, JacksonConfiguration.class, MuonTestConfiguration.class, MongoConfiguration.class, SagaFactoryTest.class, SagaConfiguration.class})
//@Configuration
public class SagaFactoryTest {

//	@Autowired
//	private SagaFactory sagaFactory;
//
//	@Test
//	public void canCreateSagaAndWaitForCompletion() throws Exception {
//		TestSaga saga = sagaFactory.create(TestSaga.class, new TestEvent()).waitForCompletion(TimeUnit.MINUTES, 1);
//
//		assertNotNull(saga);
//		assertTrue(saga.isComplete());
//	}
//
//
//    @Test
//    public void canCreateSagaAndBeNotified() throws Exception {
//        CountDownLatch latch = new CountDownLatch(1);
//
//        SagaMonitor<TestSaga, TestEvent> monitor = sagaFactory.create(TestSaga.class, new TestEvent());
//
//        final boolean[] finished = {false};
//
//        monitor.onFinished(saga -> {
//            latch.countDown();
//
//            finished[0] = saga.isComplete();
//        });
//
//        latch.await();
//        assertTrue(finished[0]);
//    }
//
//
//    //CONFIGURATION
//	@Bean
//	public TestSaga testSaga() {
//		return new TestSaga();
//	}
//
//
//	public static class TestEvent implements Event {
//	}
//
//	public static class TestSaga extends StatefulSaga<TestEvent> {
//
//		@Override
//		public void start(TestEvent event) {
//			end();
//		}
//	}
}
