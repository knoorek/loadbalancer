package com.forkbird.loadbalancer.concept;

import com.forkbird.loadbalancer.concept.strategies.LoadBalancingStrategy;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;

import java.util.List;

public class LoadBalancer {

    private final List<TargetInstance> targetInstances;
    private final LoadBalancingStrategy loadBalancingStrategy;

    public LoadBalancer(List<TargetInstance> targetInstances, LoadBalancingStrategy loadBalancingStrategy) {
        this.targetInstances = targetInstances;
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    public void handleRequest(Payload payload) {
        loadBalancingStrategy.findHostToHandlePayload(targetInstances).handleRequest(payload);
    }

    public void shutdown() {
        targetInstances.stream().forEach(targetInstance -> targetInstance.shutdown());
    }
}
