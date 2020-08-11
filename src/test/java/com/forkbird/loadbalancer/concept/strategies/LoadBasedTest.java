package com.forkbird.loadbalancer.concept.strategies;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;
import com.forkbird.loadbalancer.concept.targetinstances.TestTargetInstance;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoadBasedTest {

    @Test
    void should_pick_first_instance_on_the_list() {
        //given
        LoadBalancingStrategy<Payload> strategy = new LoadBased<>(0.8f);
        List<TargetInstance<Payload>> targetInstances = Arrays.asList(
                new TestTargetInstance("1", 0.6f),
                new TestTargetInstance("2", 0.5f),
                new TestTargetInstance("3", 0.7f));

        //when
        TargetInstance<Payload> targetInstance = strategy.findTargetInstanceToHandlePayload(targetInstances);

        //then
        assertEquals(targetInstances.get(0), targetInstance);
    }

    @Test
    void should_pick_lowest_loaded_instance_when_all_above_desired_load() {
        //given
        LoadBalancingStrategy<Payload> strategy = new LoadBased<>();
        List<TargetInstance<Payload>> targetInstances = Arrays.asList(
                new TestTargetInstance("1", 0.76f),
                new TestTargetInstance("2", 0.75f),
                new TestTargetInstance("3", 0.77f));

        //when
        TargetInstance<Payload> targetInstance = strategy.findTargetInstanceToHandlePayload(targetInstances);

        //then
        assertEquals(targetInstances.get(1), targetInstance);
    }

    @Test
    void should_pick_first_instance_under_desired_load_when_some_are_above_desired_load() {
        //given
        LoadBalancingStrategy<Payload> strategy = new LoadBased<>();
        List<TargetInstance<Payload>> targetInstances = Arrays.asList(
                new TestTargetInstance("1", 0.76f),
                new TestTargetInstance("2", 0.75f),
                new TestTargetInstance("3", 0.22f),
                new TestTargetInstance("4", 0.11f));

        //when
        TargetInstance<Payload> targetInstance = strategy.findTargetInstanceToHandlePayload(targetInstances);

        //then
        assertEquals(targetInstances.get(2), targetInstance);
    }
}
