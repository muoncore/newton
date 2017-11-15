package io.muoncore.newton.cluster;

public interface LockService {
	void executeAndRepeatWithLock(String name, LockedTask exec);

	interface LockedTask {
		void execute(TaskLockControl control);
	}

	public interface TaskLockControl {
		void releaseLock();
	}
}
