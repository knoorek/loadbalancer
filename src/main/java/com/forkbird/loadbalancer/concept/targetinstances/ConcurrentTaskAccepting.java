package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;

import static com.forkbird.loadbalancer.concept.targetinstances.ConcurrentTaskAccepting.PayloadRunnable;

public class ConcurrentTaskAccepting<T extends PayloadRunnable> extends ConcurrentBase<T> {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentTaskAccepting.class);

    public ConcurrentTaskAccepting(int threadPoolSize, String instanceName, Callback<T> callback, UncaughtExceptionHandler uncaughtExceptionHandler) {
        super(threadPoolSize, instanceName, callback, uncaughtExceptionHandler);
    }

    @Override
    public void handleRequest(T payload) {
        if (payloadQueue.offer(payload)) {
            payload.setHandlingTargetInstance(this);
            executor.execute(() -> {
                T peek = payloadQueue.peek();
                try {
                    peek.setHandled(true);
                    peek.run();
                } finally {
                    payloadQueue.remove(peek);
                    callback.onComplete(peek);
                }
            });
        } else {
            logger.warn(String.format("Rejected payload %s due to full queue on %s.", payload.getRequest(), instanceName));
            payload.setHandled(false);
            callback.onComplete(payload);
        }
    }

    public static abstract class PayloadRunnable extends Payload implements Runnable {

    }
}
