package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.ConcurrentBase.Callback;
import com.forkbird.loadbalancer.concept.targetinstances.ConcurrentTaskAccepting.PayloadRunnable;
import org.junit.jupiter.api.Test;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentTaskAcceptingTest {

    @Test
    void should_handle_payload() throws InterruptedException {
        //given
        BlockingQueue<PayloadRunnable> handledPayloads = new ArrayBlockingQueue<>(1);
        TargetInstance<PayloadRunnable> targetInstance = new ConcurrentTaskAccepting<>(1, "instance", createCallback(handledPayloads), (t, e) -> {
        });
        try {
            //when
            targetInstance.handleRequest(createPayload("hello"));
            Payload handledPayload = handledPayloads.poll(3, TimeUnit.SECONDS);

            //then
            assertNotNull(handledPayload);
            assertEquals("Responding to: hello", handledPayload.getResponse());
            assertEquals(targetInstance, handledPayload.getHandlingTargetInstance());
        } finally {
            targetInstance.shutdown();
        }
    }

    @Test
    void should_reject_execution_when_thread_pool_exceeded() throws InterruptedException {
        //given
        BlockingQueue<PayloadRunnable> handledPayloads = new ArrayBlockingQueue<>(2);
        TargetInstance<PayloadRunnable> targetInstance = new ConcurrentTaskAccepting<>(1, "instance", createCallback(handledPayloads), (t, e) -> {
        });
        try {
            //when
            targetInstance.handleRequest(createPayload("hello1"));
            targetInstance.handleRequest(createPayload("hello2"));
            Payload payload1 = handledPayloads.poll(3, TimeUnit.SECONDS);
            Payload payload2 = handledPayloads.poll(3, TimeUnit.SECONDS);

            List<Payload> outcome = Arrays.asList(payload1, payload2);
            payload1 = outcome.stream()
                    .filter(Payload::isHandled)
                    .findFirst()
                    .get();
            payload2 = outcome.stream()
                    .filter(payload -> !payload.isHandled())
                    .findFirst()
                    .get();

            //then
            assertNotNull(payload1);
            assertEquals("Responding to: hello1", payload1.getResponse());
            assertEquals(targetInstance, payload1.getHandlingTargetInstance());
            assertTrue(payload1.isHandled());

            assertNotNull(payload2);
            assertNull(payload2.getResponse());
            assertNull(payload2.getHandlingTargetInstance());
            assertFalse(payload2.isHandled());
        } finally {
            targetInstance.shutdown();
        }
    }

    @Test
    void should_calculate_load_properly() {
        //given
        BlockingQueue<PayloadRunnable> handledPayloads = new ArrayBlockingQueue<>(3);
        TargetInstance<PayloadRunnable> targetInstance = new ConcurrentTaskAccepting<>(4, "instance", createCallback(handledPayloads), (t, e) -> {
        });
        try {
            //when
            targetInstance.handleRequest(createPayload("hello"));
            targetInstance.handleRequest(createPayload("hello"));
            targetInstance.handleRequest(createPayload("hello"));

            //then
            assertEquals(new Float(0.75f), targetInstance.getLoad());
        } finally {
            targetInstance.shutdown();
        }
    }

    @Test
    void should_handle_exception_in_payload_handling() throws InterruptedException {
        //given
        BlockingQueue<PayloadRunnable> handledPayloads = new ArrayBlockingQueue<>(2);
        TestExceptionHandler uncaughtExceptionHandler = new TestExceptionHandler();
        TargetInstance<PayloadRunnable> targetInstance = new ConcurrentTaskAccepting<>(1, "instance", createCallback(handledPayloads), uncaughtExceptionHandler);
        try {
            //when
            targetInstance.handleRequest(createPayload("fail me"));
            Payload payload1 = handledPayloads.poll(3, TimeUnit.SECONDS);
            targetInstance.handleRequest(createPayload("I'm fine"));
            Payload payload2 = handledPayloads.poll(3, TimeUnit.SECONDS);

            //then
            assertNotNull(payload1);
            assertNull(payload1.getResponse());
            assertEquals(targetInstance, payload1.getHandlingTargetInstance());
            assertEquals(IllegalArgumentException.class, uncaughtExceptionHandler.getExpected().getClass());
            assertEquals("fail me", uncaughtExceptionHandler.getExpected().getMessage());

            assertNotNull(payload2);
            assertEquals("Responding to: I'm fine", payload2.getResponse());
            assertEquals(targetInstance, payload2.getHandlingTargetInstance());
        } finally {
            targetInstance.shutdown();
        }
    }

    private PayloadRunnable createPayload(String request) {
        PayloadRunnable payload = new PayloadRunnable() {
            @Override
            public void run() {
                if (request.equals("fail me")) {
                    throw new IllegalArgumentException("fail me");
                }
                setResponse(String.format("Responding to: %s", request));
            }
        };
        payload.setRequest(request);
        return payload;
    }

    private Callback<PayloadRunnable> createCallback(BlockingQueue<PayloadRunnable> handledPayloads) {
        return handledPayloads::offer;
    }

    private static class TestExceptionHandler implements UncaughtExceptionHandler {

        private Throwable expected;

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (expected == null) {
                expected = e;
            } else {
                throw new IllegalStateException("Only one exception is expected");
            }
        }

        Throwable getExpected() {
            return expected;
        }
    }

}