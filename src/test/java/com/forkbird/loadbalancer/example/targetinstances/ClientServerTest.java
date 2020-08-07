package com.forkbird.loadbalancer.example.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.ConcurrentBase.Callback;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;
import com.forkbird.loadbalancer.example.TestServer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientServerTest {

    private static int serverPort = 8080;
    private String hostName = "localhost";

    @Test
    void should_receive_response_from_server() throws InterruptedException {
        //given
        TestServer testServer = startTestServer(0);
        BlockingQueue<Payload> handledPayloads = new ArrayBlockingQueue<>(1);
        try {
            TargetInstance<Payload> targetInstance = new ClientServer("instance", 1, hostName, serverPort, 3000, createCallback(handledPayloads));
            Payload payload = new Payload();
            payload.setRequest("hello");

            //when
            targetInstance.handleRequest(payload);
            Payload handledPayload = handledPayloads.poll(3, TimeUnit.SECONDS);

            //then
            assertNotNull(handledPayload);
            assertEquals("Echo from server listening on port 8080: hello", handledPayload.getResponse());
            assertEquals(targetInstance, handledPayload.getHandlingTargetInstance());
        } finally {
            testServer.shutdown();
        }
    }

    private Callback<Payload> createCallback(BlockingQueue<Payload> handledPayloads) {
        return handledPayloads::offer;
    }

    private TestServer startTestServer(int delay) {
        TestServer testServer = new TestServer(serverPort, delay);
        new Thread(testServer).start();
        testServer.waitForStart();
        return testServer;
    }
}