package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;

public interface TargetInstance<T extends Payload> {

    void handleRequest(T payload);

    float getLoad();

    void shutdown();

}
