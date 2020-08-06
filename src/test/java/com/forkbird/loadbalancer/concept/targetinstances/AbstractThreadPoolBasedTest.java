package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.AbstractThreadPoolBased.Callback;
import org.junit.jupiter.api.Test;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AbstractThreadPoolBasedTest {

    @Test
    void should_handle_payload() throws InterruptedException {
        //given
        BlockingQueue<Payload> handledPayloads = new ArrayBlockingQueue<>(1);
        TargetInstance targetInstance = createTargetInstance(1, 0, createCallback(handledPayloads), (t, e) -> {
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
        BlockingQueue<Payload> handledPayloads = new ArrayBlockingQueue<>(2);
        TargetInstance targetInstance = createTargetInstance(1, 0, createCallback(handledPayloads), (t, e) -> {
        });
        try {
            //when
            targetInstance.handleRequest(createPayload("hello1"));
            targetInstance.handleRequest(createPayload("hello2"));
            Payload payload1 = handledPayloads.poll(3, TimeUnit.SECONDS);
            Payload payload2 = handledPayloads.poll(3, TimeUnit.SECONDS);

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
        BlockingQueue<Payload> handledPayloads = new ArrayBlockingQueue<>(3);
        TargetInstance targetInstance = createTargetInstance(4, Integer.MAX_VALUE, createCallback(handledPayloads), (t, e) -> {
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
        BlockingQueue<Payload> handledPayloads = new ArrayBlockingQueue<>(2);
        TestExceptionHandler uncaughtExceptionHandler = new TestExceptionHandler();
        TargetInstance targetInstance = createTargetInstance(createCallback(handledPayloads), uncaughtExceptionHandler);
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

    private TargetInstance createTargetInstance(int threadPoolSize, int handlingDelay, Callback callback, UncaughtExceptionHandler handler) {
        return new AbstractThreadPoolBased("instanceName", threadPoolSize, callback, handler) {
            @Override
            protected void doHandleRequest(Payload payload) {
                payload.setResponse(String.format("Responding to: %s", payload.getRequest()));
                try {
                    Thread.sleep(handlingDelay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private TargetInstance createTargetInstance(Callback callback, TestExceptionHandler uncaughtExceptionHandler) {
        return new AbstractThreadPoolBased("instanceName", 2, callback, uncaughtExceptionHandler) {
            @Override
            protected void doHandleRequest(Payload payload) {
                if (payload.getRequest().equals("fail me")) {
                    throw new IllegalArgumentException("fail me");
                } else {
                    payload.setResponse(String.format("Responding to: %s", payload.getRequest()));
                }
            }
        };
    }

    private Payload createPayload(String hello) {
        Payload payload = new Payload();
        payload.setRequest(hello);
        return payload;
    }

    private Callback createCallback(BlockingQueue<Payload> handledPayloads) {
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