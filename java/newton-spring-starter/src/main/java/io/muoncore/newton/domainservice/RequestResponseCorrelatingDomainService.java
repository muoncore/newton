package io.muoncore.newton.domainservice;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.StreamSubscriptionManager;
import io.muoncore.newton.streams.BaseStreamSubscriber;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Allow the recreation of request/ response semantics over an event interchange via a correlation mechanism in the events
 *
 * To implement, create a child of this class, add your RPC methods that internally invoke #run
 *nClusterAwareTrackingSubscriptionManager
 * public CompletableFuture<ResponseEv> getDoStuff(RequestEvent ev) {
 *   return rpcEvent(new ResponseEv("h"), ResponseEv.class, response -> { response.getId().equals("123455") });
 * }
 */
//public class RequestResponseCorrelatingDomainService extends BaseStreamSubscriber {
//
//  @Autowired
//  public RequestResponseCorrelatingDomainService(StreamSubscriptionManager streamSubscriptionManager) {
//    super(streamSubscriptionManager);
//  }
//
//  @Override
//  protected Consumer<Consumer<NewtonEvent>> run(String stream) {
//    return consumer -> {
//      streamSubscriptionManager.globallyUniqueSubscription(getClass().getSimpleName() + "-" + stream, stream, consumer);
//    };
//  }
//
//  protected <T extends NewtonEvent> CompletableFuture<T> rpcEvent(NewtonEvent requestEvent, Class<T> type, Predicate<T> filter) {
//    CompletableFuture<T> future = new CompletableFuture<>();
//
//    return future;
//  }
//
////  public CompletableFuture<WibbleEv> getWibble() {
////    return rpcEvent(new WibbleEv("h"), WibbleEv.class, newtonEvent -> {});
////  }
//
//
//  @Getter
//  @AllArgsConstructor
//  static class WibbleEv implements NewtonEvent<String> {
//    private String id;
//  }
//}
