package io.muoncore.newton.eventsource;

public enum EventProcessProtection {
	/**
	 * No locking will be enforced. This means that if two instances of this component exist within the system,
	 * both of them will receive every event. This will cause duplicate event processing. If you are sharing
	 * a resource between instances, then make sure your writes are idempotent if using this option.
	 */
	NONE,
	/**
	 * A global lock will be created around this component. Only one instance will be active at a particular time.
	 * This should be used when you have a shared resource and non idempotent writes.
	 */
	GLOBAL_LOCK
	//, ID_HASH - use an has of the id to allocate the event to different buckets allocated to services.
}
