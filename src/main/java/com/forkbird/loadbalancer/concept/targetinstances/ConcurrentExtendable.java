package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public abstract class ConcurrentExtendable<T extends Payload> extends ConcurrentBase<T> {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentExtendable.class);

    public ConcurrentExtendable(String instanceName, int threadPoolSize, Callback<T> callback, UncaughtExceptionHandler uncaughtExceptionHandler) {
        super(threadPoolSize, instanceName, callback, uncaughtExceptionHandler);
    }

    public ConcurrentExtendable(ExecutorService executor, Callback<T> callback, String instanceName, BlockingQueue<T> payloadQueue) {
        super(executor, callback, instanceName, payloadQueue);
    }

    @Override
    public void handleRequest(T payload) {
        if (payloadQueue.offer(payload)) {
            payload.setHandlingTargetInstance(this);
            executor.execute(() -> {
                T peek = null;
                try {
                    peek = payloadQueue.peek();
                    peek.setHandled(true);
                    doHandleRequest(peek);
                } finally {
                    callback.onComplete(peek);
                    payloadQueue.remove(peek);
                }
            });
        } else {
            logger.warn(String.format("Rejected payload %s due to full queue on %s.", payload.getRequest(), instanceName));
            payload.setHandled(false);
            callback.onComplete(payload);
        }
    }

    protected abstract void doHandleRequest(T payload);
}
