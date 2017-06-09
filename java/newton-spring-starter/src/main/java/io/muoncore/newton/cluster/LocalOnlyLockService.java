package io.muoncore.newton.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class LocalOnlyLockService implements LockService {

  private Executor pool = Executors.newFixedThreadPool(100);

  @Override
  public void executeAndRepeatWithLock(String name, LockedTask exec) {
    log.info("Starting to wait on the lock " + name);

    pool.execute(() -> {
      log.info("In executor " + name);
      Lock lock = new ReentrantLock();

      while(true) {
        log.info("Waiting on the lock " + name);

        lock.lock();
        CountDownLatch latch = new CountDownLatch(1);
        try {
          log.info("Obtained global lock '{}', executing local task on this node", name);
          exec.execute(() -> {
            lock.unlock();
            latch.countDown();
          });
          latch.await();
        } catch (Exception ex) {
          log.warn("Locked process has failed with an exception, and {} has been unlocked", name);
          log.warn("Locking Process failed with exception", ex);
        } finally {
          lock.unlock();
        }
      }
    });
  }
}
