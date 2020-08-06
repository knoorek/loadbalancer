package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.example.targetinstances.ClientServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Duration;
import java.util.concurrent.*;

public abstract class AbstractThreadPoolBased implements TargetInstance {

    public static final Duration EXECUTOR_SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);
    protected static final UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLERHANDLER = (t, e) -> e.printStackTrace();

    private static final Logger logger = LoggerFactory.getLogger(ClientServer.class);

    private final ExecutorService executor;
    private final String instanceName;

    private BlockingQueue<Payload> payloadQueue;

    public AbstractThreadPoolBased(String instanceName, int threadPoolSize) {
        this(instanceName, threadPoolSize, UNCAUGHT_EXCEPTION_HANDLERHANDLER);
    }

    public AbstractThreadPoolBased(String instanceName, int threadPoolSize, UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.instanceName = instanceName;
        this.payloadQueue = new ArrayBlockingQueue<>(threadPoolSize);
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            return t;
        });
    }

    @Override
    public void handleRequest(Payload payload) {
        if (payloadQueue.offer(payload)) {
            payload.setHandlingTargetInstance(this);
            executor.execute(() -> {
                try {
                    doHandleRequest(payloadQueue.take());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            logger.warn(String.format("Rejected payload %s due to full queue on %s.", payload.getRequest(), instanceName));
        }
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

    @Override
    public float getLoad() {
        return (float) payloadQueue.size() / (payloadQueue.remainingCapacity() + payloadQueue.size());
    }

    protected abstract void doHandleRequest(Payload payload);
}
