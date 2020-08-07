package com.forkbird.loadbalancer.example;

import com.forkbird.loadbalancer.concept.LoadBalancer;
import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.strategies.RoundRobin;
import com.forkbird.loadbalancer.concept.targetinstances.ConcurrentBase.Callback;
import com.forkbird.loadbalancer.example.targetinstances.ClientServer;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Example {

    public static void main(String[] args) throws InterruptedException {
        int port = 8080;
        TestServer testServer = startTestServer(port);
        BlockingQueue<Payload> payloads = new ArrayBlockingQueue<>(1);
        LoadBalancer<Payload> loadBalancer = createPayloadLoadBalancer(port, payloads::offer);

        try {

            loadBalancer.handleRequest(createPayload("payload1"));
            Payload handledPayload = payloads.poll(3, TimeUnit.SECONDS);
            System.out.printf("%s: %s%n", handledPayload.getHandlingTargetInstance(), handledPayload.getResponse());

            loadBalancer.handleRequest(createPayload("payload2"));
            handledPayload = payloads.poll(3, TimeUnit.SECONDS);
            System.out.printf("%s: %s%n", handledPayload.getHandlingTargetInstance(), handledPayload.getResponse());

        } finally {
            testServer.shutdown();
            loadBalancer.shutdown();
        }
    }

    private static LoadBalancer<Payload> createPayloadLoadBalancer(int port, Callback<Payload> callback) {
        return new LoadBalancer<>(
                    Arrays.asList(
                            new ClientServer("instance1", 1, "localhost", port, 3000, callback),
                            new ClientServer("instance2", 1, "localhost", port, 3000, callback),
                            new ClientServer("instance3", 1, "localhost", port, 3000, callback)),
                    new RoundRobin<>());
    }

    private static TestServer startTestServer(int port) {
        TestServer testServer = new TestServer(port, 0);
        new Thread(testServer).start();
        testServer.waitForStart();
        return testServer;
    }

    private static Payload createPayload(String message) {
        Payload payload = new Payload();
        payload.setRequest(message);
        return payload;
    }
}