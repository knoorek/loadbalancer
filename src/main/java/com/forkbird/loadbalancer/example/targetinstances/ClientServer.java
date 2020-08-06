package com.forkbird.loadbalancer.example.targetinstances;

import com.forkbird.loadbalancer.concept.Payload;
import com.forkbird.loadbalancer.concept.targetinstances.AbstractThreadPoolBased;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientServer extends AbstractThreadPoolBased {

    private final String instanceName;
    private final String targetHost;
    private final int targetHostPort;
    private final int timeout;

    public ClientServer(String instanceName, int threadPoolSize, String targetHost, int targetHostPort, int timeout) {
        this(instanceName, threadPoolSize, targetHost, targetHostPort, timeout, UNCAUGHT_EXCEPTION_HANDLERHANDLER);
    }

    public ClientServer(String instanceName, int threadPoolSize, String targetHost, int targetHostPort, int timeout, UncaughtExceptionHandler uncaughtExceptionHandler) {
        super(instanceName, threadPoolSize, uncaughtExceptionHandler);
        this.instanceName = instanceName;
        this.targetHost = targetHost;
        this.targetHostPort = targetHostPort;
        this.timeout = timeout;
    }

    @Override
    protected void doHandleRequest(Payload payload) {
        try (
                Socket socket = new Socket(targetHost, targetHostPort);
                PrintWriter targetOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader targetIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            socket.setSoTimeout(timeout);
            targetOut.println(payload.getRequest());
            payload.setResponse(targetIn.readLine());
        } catch (UnknownHostException e) {
            throw new RuntimeException(String.format("Error sending %s with %s", payload, this), e);
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
