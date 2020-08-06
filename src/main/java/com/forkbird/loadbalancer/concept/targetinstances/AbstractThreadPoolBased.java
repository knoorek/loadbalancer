package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.example.targetinstances.ClientServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Duration;
import java.util.concurrent.*;

public abstract class AbstractThreadPoolBased implements TargetInstance {

    private static final Duration EXECUTOR_SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);
    protected static final UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = (t, e) -> e.printStackTrace();

    private static final Logger logger = LoggerFactory.getLogger(ClientServer.class);

    private final ExecutorService executor;
    private final Callback callback;
    private final String instanceName;

    private BlockingQueue<Payload> payloadQueue;

    public AbstractThreadPoolBased(String instanceName, int threadPoolSize, Callback callback, UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.instanceName = instanceName;
        this.payloadQueue = new ArrayBlockingQueue<>(threadPoolSize);
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            return t;
        });
        this.callback = callback;
    }

    @Override
    public void handleRequest(Payload payload) {
        if (payloadQueue.offer(payload)) {
            payload.setHandlingTargetInstance(this);
            executor.execute(() -> {
                Payload p = null;
                try {
                    p = payloadQueue.peek();
                    p.setHandled(true);
                    doHandleRequest(p);
                } finally {
                    callback.onComplete(p);
                    payloadQueue.remove(p);
                }
            });
        } else {
            logger.warn(String.format("Rejected payload %s due to full queue on %s.", payload.getRequest(), instanceName));
            payload.setHandled(false);
            callback.onComplete(payload);
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

    public interface Callback {
        void onComplete(Payload payload);
    }
}
