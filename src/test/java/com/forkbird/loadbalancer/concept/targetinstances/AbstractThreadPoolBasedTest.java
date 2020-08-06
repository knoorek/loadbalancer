package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractThreadPoolBasedTest {

    @Test
    void should_handle_payload() throws InterruptedException {
        //given
        TargetInstance targetInstance = createTargetInstance(1, 0, (t, e) -> {
        });
        try {
            Payload payload = new Payload();
            payload.setRequest("hello");

            //when
            targetInstance.handleRequest(payload);
            Thread.sleep(1000);

            //then
            assertEquals("Responding to: hello", payload.getResponse());
            assertEquals(targetInstance, payload.getHandlingTargetInstance());
        } finally {
            targetInstance.shutdown();
        }
    }

    @Test
    void should_reject_execution_when_thread_pool_exceeded() throws InterruptedException {
        //given
        TargetInstance targetInstance = createTargetInstance(2, 0, (t, e) -> {
        });
        try {
            Payload payload1 = createPayload("hello1");
            Payload payload2 = createPayload("hello2");
            Payload payload3 = createPayload("hello3");

            //when
            targetInstance.handleRequest(payload1);
            targetInstance.handleRequest(payload2);
            targetInstance.handleRequest(payload3);
            Thread.sleep(1000);

            //then
            assertEquals("Responding to: hello1", payload1.getResponse());
            assertEquals(targetInstance, payload1.getHandlingTargetInstance());
            assertEquals("Responding to: hello2", payload2.getResponse());
            assertEquals(targetInstance, payload2.getHandlingTargetInstance());
            assertNull(payload3.getResponse());
            assertNull(payload3.getHandlingTargetInstance());
        } finally {
            targetInstance.shutdown();
        }
    }

    @Test
    void should_calculate_load_properly() throws InterruptedException {
        //given
        TargetInstance targetInstance = createTargetInstance(4, 1000, (t, e) -> {
        });
        try {
            //when
            targetInstance.handleRequest(createPayload("hello"));
            targetInstance.handleRequest(createPayload("hello"));
            targetInstance.handleRequest(createPayload("hello"));

            //then
            assertTrue(new Float(0.75f).equals(targetInstance.getLoad()));
        } finally {
            targetInstance.shutdown();
        }
    }

    @Test
    void should_handle_exception_in_handling() throws InterruptedException {
        //given
        TestExceptionHandler uncaughtExceptionHandler = new TestExceptionHandler();
        TargetInstance targetInstance = createTargetInstance(uncaughtExceptionHandler);
        try {
            Payload payload1 = createPayload("fail me");
            Payload payload2 = createPayload("I'm fine");

            //when
            targetInstance.handleRequest(payload1);
            targetInstance.handleRequest(payload2);
            Thread.sleep(1000);

            //then
            assertNull(payload1.getResponse());
            assertEquals(targetInstance, payload1.getHandlingTargetInstance());
            assertEquals(IllegalArgumentException.class, uncaughtExceptionHandler.getExpected().getClass());
            assertEquals("fail me", uncaughtExceptionHandler.getExpected().getMessage());

            assertEquals("Responding to: I'm fine", payload2.getResponse());
            assertEquals(targetInstance, payload2.getHandlingTargetInstance());
        } finally {
            targetInstance.shutdown();
        }
    }

    private TargetInstance createTargetInstance(int threadPoolSize, int handlingDelay, Thread.UncaughtExceptionHandler handler) {
        return new AbstractThreadPoolBased("instanceName", threadPoolSize, handler) {
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

    private TargetInstance createTargetInstance(TestExceptionHandler uncaughtExceptionHandler) {
        return new AbstractThreadPoolBased("instanceName", 2, uncaughtExceptionHandler) {
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

    private class TestExceptionHandler implements Thread.UncaughtExceptionHandler {

        private Throwable expected;

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (expected == null) {
                expected = e;
            } else {
                throw new IllegalStateException("Only one exception is expected");
            }
        }

        public Throwable getExpected() {
            return expected;
        }
    }
}