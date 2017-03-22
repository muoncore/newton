package io.muoncore.newton.cluster;

import lombok.extern.slf4j.Slf4j;
import org.jgroups.JChannel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

@Slf4j
public class JGroupsLockService implements LockService {

	private org.jgroups.blocks.locking.LockService lockService;

	private Executor pool = Executors.newFixedThreadPool(100);

	public JGroupsLockService() throws Exception {

		JChannel ch = new JChannel(JGroupsLockService.class.getResourceAsStream("/jgroups.xml"));
		lockService = new org.jgroups.blocks.locking.LockService(ch);
		ch.connect("cibecs-cluster");
	}

	@Override
	public void executeAndRepeatWithLock(String name, LockedTask exec) {

		pool.execute(() -> {
			Lock lock = lockService.getLock(name);

			while(true) {
				log.debug("Waiting on the lock " + name);

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
					lock.unlock();
				}
			}
		});
	}
}
