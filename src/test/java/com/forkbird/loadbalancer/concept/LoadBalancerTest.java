package com.forkbird.loadbalancer.concept;

import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;
import com.forkbird.loadbalancer.concept.targetinstances.TestTargetInstance;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoadBalancerTest {

    @Test
    void should_load_balance_to_one_target_host() {
        LoadBalancer loadBalancer = null;
        try {
            //given
            TargetInstance targetInstance = new TestTargetInstance("instanceName");
            loadBalancer = new LoadBalancer(Collections.singletonList(targetInstance), targetHosts -> targetHosts.get(0));
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