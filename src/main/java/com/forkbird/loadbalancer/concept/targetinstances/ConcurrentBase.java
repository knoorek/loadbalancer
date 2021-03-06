package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.*;

import static java.lang.Thread.UncaughtExceptionHandler;

public abstract class ConcurrentBase<T extends Payload> implements TargetInstance<T> {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentBase.class);
    protected static final UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = (t, e) -> e.printStackTrace();
    private static final Duration EXECUTOR_SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);

    protected final ExecutorService executor;
    protected final Callback<T> callback;
    protected final String instanceName;
    protected final BlockingQueue<T> payloadQueue;

    public ConcurrentBase(int threadPoolSize, String instanceName, Callback<T> callback, UncaughtExceptionHandler uncaughtExceptionHandler) {
        this(new ThreadPoolExecutor(threadPoolSize, threadPoolSize,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(threadPoolSize),
                        r -> {
                            Thread t = new Thread(r);
                            t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                            return t;
                        }),
                callback,
                instanceName,
                new ArrayBlockingQueue<>(threadPoolSize));
    }

    public ConcurrentBase(ExecutorService executor, Callback<T> callback, String instanceName, BlockingQueue<T> payloadQueue) {
        this.executor = executor;
        this.callback = callback;
        this.instanceName = instanceName;
        this.payloadQueue = payloadQueue;
    }

    @Override
    public float getLoad() {
        return (float) payloadQueue.size() / (payloadQueue.remainingCapacity() + payloadQueue.size());
    }

    @Override
    public void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                    logger.warn("Executor did not terminate");
                }
            } catch (InterruptedException ie) {
                throw new IllegalStateException(ie);
            }
        }
    }

    public interface Callback<T> {
        void onComplete(T payload);
    }
}
