package com.forkbird.loadbalancer.concept;

import com.forkbird.loadbalancer.concept.strategies.LoadBased;
import com.forkbird.loadbalancer.concept.strategies.RoundRobin;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;
import com.forkbird.loadbalancer.concept.targetinstances.TestTargetInstance;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoadBalancerE2ETest {

    @Test
    void should_select_target_host_with_round_robin_strategy() {
        LoadBalancer<Payload> loadBalancer = null;
        try {
            //given
            TargetInstance<Payload> expected = new TestTargetInstance("1", 0.6f);
            List<TargetInstance<Payload>> targetInstances = Arrays.asList(
                    expected,
                    new TestTargetInstance("2", 0.5f),
                    new TestTargetInstance("3", 0.2f),
                    new TestTargetInstance("4", 0.1f));
            loadBalancer = new LoadBalancer<>(targetInstances, new RoundRobin<>());
            Payload payload = new Payload();

            //when
            loadBalancer.handleRequest(payload);

            //then
            assertEquals(expected, payload.getHandlingTargetInstance());
        } finally {
            if (loadBalancer != null) {
                loadBalancer.shutdown();
            }
        }
    }

    @Test
    void should_select_target_host_with_load_based_strategy() {
        LoadBalancer<Payload> loadBalancer = null;
        try {
            //given
            TargetInstance<Payload> expected = new TestTargetInstance("1", 0.76f);
            List<TargetInstance<Payload>> targetInstances = Arrays.asList(
                    new TestTargetInstance("2", 0.78f),
                    new TestTargetInstance("3", 0.8f),
                    expected,
                    new TestTargetInstance("4", 0.9f));
            loadBalancer = new LoadBalancer<>(targetInstances, new LoadBased<>());
            Payload payload = new Payload();

            //when
            loadBalancer.handleRequest(payload);

            //then
            assertEquals(expected, payload.getHandlingTargetInstance());
        } finally {
            if (loadBalancer != null) {
                loadBalancer.shutdown();
            }
        }
    }
}
