package com.forkbird.loadbalancer.example.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.ConcurrentExtendable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Socket;

public class ClientServer extends ConcurrentExtendable<Payload> {

    private final String targetHost;
    private final int targetHostPort;
    private final int timeout;

    public ClientServer(String instanceName, int threadPoolSize, String targetHost, int targetHostPort, int timeout, Callback<Payload> callback) {
        this(instanceName, threadPoolSize, targetHost, targetHostPort, timeout, callback, UNCAUGHT_EXCEPTION_HANDLER);
    }

    public ClientServer(String instanceName,
                        int threadPoolSize,
                        String targetHost,
                        int targetHostPort,
                        int timeout,
                        Callback<Payload> callback,
                        UncaughtExceptionHandler uncaughtExceptionHandler) {
        super(instanceName, threadPoolSize, callback, uncaughtExceptionHandler);
        this.targetHost = targetHost;
        this.targetHostPort = targetHostPort;
        this.timeout = timeout;
    }

    @Override
    protected void doHandleRequest(Payload payload) {
        try (
                Socket socket = new Socket(targetHost, targetHostPort);
                PrintWriter targetOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader targetIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            socket.setSoTimeout(timeout);
            targetOut.println(payload.getRequest());
            payload.setResponse(targetIn.readLine());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error sending %s with %s", payload, this), e);
        }
    }

    @Override
    public String toString() {
        return "ClientServer{" +
                "instanceName='" + instanceName + '\'' +
                '}';
    }
}
