package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;

public interface TargetInstance {

    void handleRequest(Payload payload);

    float getLoad();

    void shutdown();

}
