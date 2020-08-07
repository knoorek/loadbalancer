package com.forkbird.loadbalancer.concept.strategies;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;

import java.util.List;

public class RoundRobin<T extends Payload> implements LoadBalancingStrategy<T> {

    private int lastTargetHostIndex;

    @Override
    public synchronized TargetInstance<T> findHostToHandlePayload(List<TargetInstance<T>> targetInstances) {
        TargetInstance<T> targetInstance = targetInstances.get(lastTargetHostIndex);
        lastTargetHostIndex = lastTargetHostIndex == targetInstances.size() - 1 ? 0 : ++lastTargetHostIndex;
        return targetInstance;
    }
}
