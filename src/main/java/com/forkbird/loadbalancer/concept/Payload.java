package com.forkbird.loadbalancer.concept;

import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;

public class Payload {

    private TargetInstance handlingTargetInstance;
    private String request;
    private String response;
    private boolean handled;

    public TargetInstance getHandlingTargetInstance() {
        return handlingTargetInstance;
    }

    public void setHandlingTargetInstance(TargetInstance handlingTargetInstance) {
        this.handlingTargetInstance = handlingTargetInstance;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    @Override
    public String toString() {
        return "Payload{" +
                "request='" + request + '\'' +
                '}';
    }
}
