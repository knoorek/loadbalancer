package com.forkbird.loadbalancer.example.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;
import com.forkbird.loadbalancer.example.TestServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientServerTest {

    private static int serverPort = 8080;
    private String hostName = "localhost";

    @Test
    void should_receive_response_from_server() throws InterruptedException {
        //given
        TestServer testServer = startTestServer(0);
        try {
            TargetInstance targetInstance = new ClientServer("instance", 1, hostName, serverPort, 3000);
            Payload payload = new Payload();
            payload.setRequest("hello");

            //when
            targetInstance.handleRequest(payload);
            Thread.sleep(1000);

            //then
            assertEquals("Echo from server listening on port 8080: hello", payload.getResponse());
            assertEquals(targetInstance, payload.getHandlingTargetInstance());
        } finally {
            testServer.shutdown();
        }
    }

    private TestServer startTestServer(int delay) {
        TestServer testServer = new TestServer(serverPort, delay);
        new Thread(testServer).start();
        testServer.waitForStart();
        return testServer;
    }
}