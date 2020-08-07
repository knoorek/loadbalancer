package com.forkbird.loadbalancer.concept;

import com.forkbird.loadbalancer.concept.strategies.LoadBalancingStrategy;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;
import com.forkbird.loadbalancer.concept.targetinstances.TestTargetInstance;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoadBalancerTest {

    @Test
    void should_load_balance_to_one_target_host() {
        LoadBalancer<Payload> loadBalancer = null;
        try {
            //given
            TargetInstance<Payload> targetInstance = new TestTargetInstance("instanceName");
            List<TargetInstance<Payload>> targetInstances = Collections.singletonList(targetInstance);
            LoadBalancingStrategy<Payload> loadBalancingStrategy = targetInstancesList -> targetInstancesList.get(0);
            loadBalancer = new LoadBalancer<>(targetInstances, loadBalancingStrategy);
            Payload payload = new Payload();

            //when
            loadBalancer.handleRequest(payload);

            //then
            assertEquals(payload.getHandlingTargetInstance(), targetInstance);
        } finally {
            if (loadBalancer != null) {
                loadBalancer.shutdown();
            }
        }
    }
}