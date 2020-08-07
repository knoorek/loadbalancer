package com.forkbird.loadbalancer.concept.strategies;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;
import com.forkbird.loadbalancer.concept.targetinstances.TestTargetInstance;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoundRobinTest {

    @Test
    void should_round_robin_between_two_hosts() {
        //given
        LoadBalancingStrategy<Payload> strategy = new RoundRobin<>();
        List<TargetInstance<Payload>> targetInstances = Arrays.asList(new TestTargetInstance("1"), new TestTargetInstance("2"));
        //when
        //then
        assertEquals(targetInstances.get(0), strategy.findHostToHandlePayload(targetInstances));
        assertEquals(targetInstances.get(1), strategy.findHostToHandlePayload(targetInstances));
        assertEquals(targetInstances.get(0), strategy.findHostToHandlePayload(targetInstances));
        assertEquals(targetInstances.get(1), strategy.findHostToHandlePayload(targetInstances));
    }
}