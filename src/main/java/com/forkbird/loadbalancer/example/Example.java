package com.forkbird.loadbalancer.example;

import com.forkbird.loadbalancer.concept.LoadBalancer;
import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.strategies.RoundRobin;
import com.forkbird.loadbalancer.example.targetinstances.ClientServer;

import java.util.Arrays;

public class Example {

    public static void main(String args[]) throws InterruptedException {
        int port = 8080;
        TestServer testServer = startTestServer(port);
        LoadBalancer loadBalancer = new LoadBalancer(
                Arrays.asList(
                        new ClientServer("instance1", 1, "localhost", port, 3000),
                        new ClientServer("instance2", 1, "localhost", port, 3000),
                        new ClientServer("instance3", 1, "localhost", port, 3000)),
                new RoundRobin());
        try {
            Payload payload = getPayload("payload");

            loadBalancer.handleRequest(payload);
            Thread.sleep(1000);
            System.out.printf("%s: %s%n", payload.getHandlingTargetInstance(), payload.getResponse());

            loadBalancer.handleRequest(payload);
            Thread.sleep(1000);
            System.out.printf("%s: %s%n", payload.getHandlingTargetInstance(), payload.getResponse());

        } finally {
            testServer.shutdown();
            loadBalancer.shutdown();
        }
    }

    private static TestServer startTestServer(int port) {
        TestServer testServer = new TestServer(port, 0);
        new Thread(testServer).start();
        testServer.waitForStart();
        return testServer;
    }

    private static Payload getPayload(String message) {
        Payload payload = new Payload();
        payload.setRequest(message);
        return payload;
    }
}