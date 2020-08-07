package com.forkbird.loadbalancer.concept;

import com.forkbird.loadbalancer.concept.strategies.LoadBalancingStrategy;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;

import java.util.List;

public class LoadBalancer<T extends Payload> {

    private final List<TargetInstance<T>> targetInstances;
    private final LoadBalancingStrategy<T> loadBalancingStrategy;

    public LoadBalancer(List<TargetInstance<T>> targetInstances, LoadBalancingStrategy<T> loadBalancingStrategy) {
        this.targetInstances = targetInstances;
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    public void handleRequest(T payload) {
        loadBalancingStrategy.findHostToHandlePayload(targetInstances).handleRequest(payload);
    }

    public void shutdown() {
        targetInstances.stream().forEach(TargetInstance::shutdown);
    }
}
