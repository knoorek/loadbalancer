package com.forkbird.loadbalancer.concept.strategies;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;

import java.util.List;

public interface LoadBalancingStrategy<T extends Payload> {

    TargetInstance<T> findTargetInstanceToHandlePayload(List<TargetInstance<T>> targetInstances);
}
