package com.forkbird.loadbalancer.concept.strategies;

import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;

import java.util.List;

public interface LoadBalancingStrategy {

    TargetInstance findHostToHandlePayload(List<TargetInstance> targetInstances);
}
