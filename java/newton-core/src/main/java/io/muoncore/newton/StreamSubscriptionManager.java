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
     *
     * Starts from the beginning of the stream and plays from that point. Once the stream is up to date and all cold data is played, hot data will be played in order
     * afterwards.
     *
     * If this replay fails, then it will be restarted somewhere in the cluster at the same point it failed.
     *
     * @param subscriptionName
     * @param stream
     * @param onData
     */
    void globallyUniqueSubscription(String subscriptionName, String stream, Consumer<NewtonEvent> onData);

    /**
     * As {@link #globallyUniqueSubscription(String, String, Consumer)}, with the difference that this version starts from the current HOT
     * location in the stream, not the beginning. After it has started, it has the same behaviour as the ohter variant.
     */
    void globallyUniqueSubscriptionFromNow(String subscriptionName, String stream, Consumer<NewtonEvent> onData);

    /**
     * Will replay to the given stream and persist the current location so that on restart/ failover
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
