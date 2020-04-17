package com.nucleus.pubsub;

@FunctionalInterface
public interface PubSubListener<M> {

    void onMessage(M message);
}
