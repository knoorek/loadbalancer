package com.forkbird.loadbalancer.concept.strategies;

import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;

import java.util.List;

public class LoadBased implements LoadBalancingStrategy {

    private final float maxDesiredLoad = 0.75f;

    @Override
    public TargetInstance findHostToHandlePayload(List<TargetInstance> targetInstances) {
        return targetInstances.stream().reduce((targetHost, targetHost2) -> {
            if (targetHost.getLoad() < maxDesiredLoad) {
                return targetHost;
            }
            return Float.compare(targetHost.getLoad(), targetHost2.getLoad()) == -1 ? targetHost : targetHost2;
        }).get();
    }

//    private TargetHost getTargetHostLambda(List<TargetHost> targetHosts) {
//        Optional<TargetHost> targetHost = targetHosts.stream()
//                .filter(th -> th.getLoad() < maxDesiredLoad).findFirst();
//        if (targetHost.isPresent()) {
//            return targetHost.get();
//        }
//        return targetHosts.stream()
//                .filter(th -> th.getLoad() >= maxDesiredLoad)
//                .min((o1, o2) -> Float.compare(o1.getLoad(), o2.getLoad())).get();
//    }
//
//    private TargetHost getTargetHostFor(List<TargetHost> targetHosts) {
//        TargetHost minLoadTargetHost = targetHosts.get(0);
//        for (TargetHost targetHost : targetHosts) {
//            if (targetHost.getLoad() < maxDesiredLoad) {
//                return targetHost;
//            } else {
//                minLoadTargetHost = Float.compare(minLoadTargetHost.getLoad(), targetHost.getLoad()) == -1 ? minLoadTargetHost : targetHost;
//            }
//        }
//        return minLoadTargetHost;
//    }
}
