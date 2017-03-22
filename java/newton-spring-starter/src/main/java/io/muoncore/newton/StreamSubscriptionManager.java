package io.muoncore.newton;

import java.util.function.Consumer;

/**
 * Provides managed streams across the cluster
 */
public interface StreamSubscriptionManager {

    /**
     * A single instance of this subscription will run in the cluster. When one node fails, another will gain the lock and
     * run from that point onwards.
     * Tracks the current position in the event stream and will continue from that point onwards.
     * @param subscriptionName
     * @param stream
     * @param onData
     */
    void globallyUniqueSubscription(String subscriptionName, String stream, Consumer<NewtonEvent> onData);

    /**
     * Will subscribe to the given stream and persist the current location so that on restart/ failover
     * the stream will be played from this location.
     *
     * This will run as many times as requested across the various instances of a service, no locking or cluster resource exclusion.
     */
    void localTrackingSubscription(String subscriptionName, String streamName, Consumer<NewtonEvent> onData);

    /**
     * Locally managed subscription that will perform a full replay whenever the subscription is created
     */
    void localNonTrackingSubscription(String streamName, Consumer<NewtonEvent> onData);

}
