package com.forkbird.loadbalancer.concept.strategies;

import com.forkbird.loadbalancer.concept.targetinstances.ConcurrentBase.Callback;
import com.forkbird.loadbalancer.concept.targetinstances.ConcurrentExtendable;
import com.forkbird.loadbalancer.concept.targetinstances.ConcurrentTaskAccepting;
import com.forkbird.loadbalancer.concept.targetinstances.ConcurrentTaskAccepting.PayloadRunnable;
import com.forkbird.loadbalancer.concept.targetinstances.TargetInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentTargetInstanceLoadTest {

    @ParameterizedTest
    @MethodSource("provideTargetInstances")
    void should_not_create_more_threads_than_thread_pool_size(TargetInstanceSupplier<PayloadRunnable> targetInstanceSupplier) throws InterruptedException {
        //given
        int maxThreadsToHandle = 100;
        int threadPoolSize = 20;
        CountDownLatch waitForAllThreadsToBeHandled = new CountDownLatch(maxThreadsToHandle);

        TestThreadFactory threadFactory = new TestThreadFactory();
        ExecutorService executor = createExecutorService(threadPoolSize, threadFactory);
        TestCallback callback = new TestCallback(waitForAllThreadsToBeHandled);
        TargetInstance<PayloadRunnable> targetInstance = targetInstanceSupplier.get(threadPoolSize, executor, callback);

        //when
        for (int i = 0; i < maxThreadsToHandle; i++) {
            targetInstance.handleRequest(new TestPayloadRunnable());
        }
        waitForAllThreadsToBeHandled.await(10, TimeUnit.SECONDS);

        //then
        assertEquals(threadPoolSize, threadFactory.getCreatedThreadsCount());
        assertTrue(callback.getHandledPayloadsCount() >= threadPoolSize);
        assertEquals(maxThreadsToHandle - callback.getRejectedPayloadsCount(), callback.getHandledPayloadsCount());
    }

    @ParameterizedTest
    @MethodSource("provideTargetInstances")
    void should_handle_exceptions_in_threads(TargetInstanceSupplier<PayloadRunnable> targetInstanceSupplier) throws InterruptedException {
        //given
        int maxThreadsToHandle = 100;
        int threadPoolSize = 20;
        CountDownLatch waitForAllThreadsToBeHandled = new CountDownLatch(maxThreadsToHandle);

        TestUncaughtExceptionHandler uncaughtExceptionHandler = new TestUncaughtExceptionHandler();
        TestThreadFactory threadFactory = new TestThreadFactory(uncaughtExceptionHandler);
        ExecutorService executor = createExecutorService(threadPoolSize, threadFactory);
        TestCallback callback = new TestCallback(waitForAllThreadsToBeHandled);
        TargetInstance<PayloadRunnable> targetInstance = targetInstanceSupplier.get(threadPoolSize, executor, callback);
        Random exceptionsRandom = new Random();

        //when
        for (int i = 0; i < maxThreadsToHandle; i++) {
            targetInstance.handleRequest(new TestPayloadRunnable(exceptionsRandom));
        }
        waitForAllThreadsToBeHandled.await(10, TimeUnit.SECONDS);

        //then
        assertTrue(callback.getHandledPayloadsCount() >= threadPoolSize);
        assertEquals(maxThreadsToHandle - callback.getRejectedPayloadsCount(), callback.getHandledPayloadsCount());
    }

    private static Stream<Arguments> provideTargetInstances() {
        return Stream.of(
                Arguments.of(new ConcurrentExtendableSupplier()),
                Arguments.of(new ConcurrentTaskAcceptingSupplier()));
    }

    private ExecutorService createExecutorService(int threadPoolSize, TestThreadFactory threadFactory) {
        return new ThreadPoolExecutor(threadPoolSize,
                threadPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory);
    }

    private static class TestThreadFactory implements ThreadFactory {

        private final UncaughtExceptionHandler uncaughtExceptionHandler;
        private AtomicInteger createdThreadsCount = new AtomicInteger(0);

        TestThreadFactory() {
            this(null);
        }

        TestThreadFactory(UncaughtExceptionHandler uncaughtExceptionHandler) {
            this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        }

        @Override
        public Thread newThread(Runnable r) {
            createdThreadsCount.incrementAndGet();
            Thread thread = new Thread(r);
            if (uncaughtExceptionHandler != null) {
                thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }
            return thread;
        }

        int getCreatedThreadsCount() {
            return createdThreadsCount.get();
        }
    }

    private class TestUncaughtExceptionHandler implements UncaughtExceptionHandler {

        private AtomicInteger exceptionsCount = new AtomicInteger(0);

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            exceptionsCount.incrementAndGet();
        }

        int getExceptionsCount() {
            return exceptionsCount.get();
        }
    }

    private class TestCallback implements Callback<PayloadRunnable> {

        private final CountDownLatch waitForAllThreadsToBeHandled;
        private AtomicInteger handledPayloadsCount = new AtomicInteger(0);
        private AtomicInteger rejectedPayloadsCount = new AtomicInteger(0);

        TestCallback(CountDownLatch waitForAllThreadsToBeHandled) {
            this.waitForAllThreadsToBeHandled = waitForAllThreadsToBeHandled;
        }

        @Override
        public void onComplete(PayloadRunnable payload) {
            if (payload.isHandled()) {
                handledPayloadsCount.incrementAndGet();
            } else {
                rejectedPayloadsCount.incrementAndGet();
            }
            waitForAllThreadsToBeHandled.countDown();
        }

        int getHandledPayloadsCount() {
            return handledPayloadsCount.get();
        }

        int getRejectedPayloadsCount() {
            return rejectedPayloadsCount.get();
        }
    }

    private static class TestPayloadRunnable extends PayloadRunnable {

        private final Random exceptionsRandom;

        TestPayloadRunnable() {
            this(null);
        }

        TestPayloadRunnable(Random exceptionsRandom) {
            this.exceptionsRandom = exceptionsRandom;
        }

        @Override
        public void run() {
            if (exceptionsRandom != null && exceptionsRandom.nextBoolean()) {
                throw new RuntimeException("random execution failure!");
            }
        }
    }

    interface TargetInstanceSupplier<T extends PayloadRunnable> {

        TargetInstance<T> get(int threadPoolSize, ExecutorService executor, Callback<PayloadRunnable> callback);
    }

    private static class ConcurrentExtendableSupplier implements TargetInstanceSupplier<PayloadRunnable> {

        @Override
        public TargetInstance<PayloadRunnable> get(int threadPoolSize, ExecutorService executor, Callback<PayloadRunnable> callback) {
            return new ConcurrentExtendable<PayloadRunnable>(
                    executor,
                    callback,
                    "instance",
                    new ArrayBlockingQueue<>(threadPoolSize)) {

                @Override
                protected void doHandleRequest(PayloadRunnable payload) {
                    payload.run();
                }
            };
        }
    }

    private static class ConcurrentTaskAcceptingSupplier implements TargetInstanceSupplier<PayloadRunnable> {

        @Override
        public TargetInstance<PayloadRunnable> get(int threadPoolSize, ExecutorService executor, Callback<PayloadRunnable> callback) {
            return new ConcurrentTaskAccepting<>(
                    executor,
                    callback,
                    "instance",
                    new ArrayBlockingQueue<>(threadPoolSize));
        }
    }
}