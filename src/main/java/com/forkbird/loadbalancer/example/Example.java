package com.forkbird.loadbalancer.example;

import com.forkbird.loadbalancer.concept.LoadBalancer;
import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.strategies.RoundRobin;
import com.forkbird.loadbalancer.concept.targetinstances.AbstractThreadPoolBased.Callback;
import com.forkbird.loadbalancer.example.targetinstances.ClientServer;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Example {

    public static void main(String[] args) throws InterruptedException {
        int port = 8080;
        BlockingQueue<Payload> payloads = new ArrayBlockingQueue<>(1);
        TestServer testServer = startTestServer(port);
        Callback callback = payloads::offer;
        LoadBalancer loadBalancer = new LoadBalancer(
                Arrays.asList(
                        new ClientServer("instance1", 1, "localhost", port, 3000, callback),
                        new ClientServer("instance2", 1, "localhost", port, 3000, callback),
                        new ClientServer("instance3", 1, "localhost", port, 3000, callback)),
                new RoundRobin());

        try {
            loadBalancer.handleRequest(getPayload("payload1"));
            Payload handledPayload = payloads.poll(3, TimeUnit.SECONDS);
            System.out.printf("%s: %s%n", handledPayload.getHandlingTargetInstance(), handledPayload.getResponse());

            loadBalancer.handleRequest(getPayload("payload2"));
            handledPayload = payloads.poll(3, TimeUnit.SECONDS);
            System.out.printf("%s: %s%n", handledPayload.getHandlingTargetInstance(), handledPayload.getResponse());

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