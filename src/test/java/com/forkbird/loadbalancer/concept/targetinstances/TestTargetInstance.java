package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;

public class TestTargetInstance implements TargetInstance<Payload> {

    private final String instanceName;
    private final float load;

    public TestTargetInstance(String instanceName) {
        this.instanceName = instanceName;
        this.load = 0.0f;
    }

    public TestTargetInstance(String instanceName, float load) {
        this.instanceName = instanceName;
        this.load = load;
    }

    @Override
    public void handleRequest(Payload payload) {
        payload.setHandlingTargetInstance(this);
    }

    @Override
    public float getLoad() {
        return load;
    }

    @Override
    public void shutdown() {
        //
    }

    @Override
    public String toString() {
        return "TestTargetInstance{" +
                "hostName='" + instanceName + '\'' +
                '}';
    }
}
