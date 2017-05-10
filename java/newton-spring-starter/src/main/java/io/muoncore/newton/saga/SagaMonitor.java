package io.muoncore.newton.saga;

import java.util.concurrent.TimeUnit;

public interface SagaMonitor<T extends Saga> {
    String getId();
    /*
     * async interface (preferred!) Will use an internal dispatch pool and not block control thread.
     * You _will_ lose current thread local state.
     */
    void onFinished(SagaListener<T> eventListener);

    /**
     * Sync interface to wait for the end of a saga. Will wait up to the given timeout, then return the saga in
     * its current state. You should check {@link Saga#isComplete()} to see if the saga has actually finished or
     * you underwent a timeout.
     */
    T waitForCompletion(TimeUnit unit, long timeout);
}
