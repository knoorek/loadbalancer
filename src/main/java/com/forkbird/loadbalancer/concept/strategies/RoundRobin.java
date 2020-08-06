package com.forkbird.loadbalancer.concept.strategies;

import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;

import java.util.List;

public class RoundRobin implements LoadBalancingStrategy {

    private int lastTargetHostIndex;

    @Override
    public synchronized TargetInstance findHostToHandlePayload(List<TargetInstance> targetInstances) {
        TargetInstance targetInstance = targetInstances.get(lastTargetHostIndex);
        lastTargetHostIndex = lastTargetHostIndex == targetInstances.size() - 1 ? 0 : ++lastTargetHostIndex;
        return targetInstance;
    }
}
