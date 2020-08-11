package com.forkbird.loadbalancer.concept.strategies;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;

import java.util.List;

public class LoadBased<T extends Payload> implements LoadBalancingStrategy<T> {

    private final float maxDesiredLoad;

    public LoadBased() {
        this.maxDesiredLoad = 0.75f;
    }

    public LoadBased(float maxDesiredLoad) {
        this.maxDesiredLoad = maxDesiredLoad;
    }

    @Override
    public TargetInstance<T> findTargetInstanceToHandlePayload(List<TargetInstance<T>> targetInstances) {
        return targetInstances.stream().reduce((targetHost, targetHost2) -> {
            if (targetHost.getLoad() < maxDesiredLoad) {
                return targetHost;
            }
            return Float.compare(targetHost.getLoad(), targetHost2.getLoad()) == -1 ? targetHost : targetHost2;
        }).get();
    }

    public float getMaxDesiredLoad() {
        return maxDesiredLoad;
    }
}
