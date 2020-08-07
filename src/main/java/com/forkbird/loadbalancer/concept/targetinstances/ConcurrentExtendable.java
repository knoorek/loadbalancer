package com.forkbird.loadbalancer.concept.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.example.targetinstances.ClientServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;

public abstract class ConcurrentExtendable<T extends Payload> extends ConcurrentBase<T> {

    private static final Logger logger = LoggerFactory.getLogger(ClientServer.class);

    public ConcurrentExtendable(String instanceName, int threadPoolSize, Callback<T> callback, UncaughtExceptionHandler uncaughtExceptionHandler) {
        super(threadPoolSize, instanceName, callback, uncaughtExceptionHandler);
    }

    @Override
    public void handleRequest(T payload) {
        if (payloadQueue.offer(payload)) {
            payload.setHandlingTargetInstance(this);
            executor.execute(() -> {
                T p = null;
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

    protected abstract void doHandleRequest(Payload payload);
}
