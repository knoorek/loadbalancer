package com.forkbird.loadbalancer.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestServer implements Runnable {

    private final int port;
    private final CountDownLatch lock = new CountDownLatch(1);
    private volatile int responseDelay;
    private ServerSocket serverSocket;

    public TestServer(int port, int responseDelay) {
        this.port = port;
        this.responseDelay = responseDelay;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (!serverSocket.isClosed()) {
                lock.countDown();
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        handleRequest(clientSocket, responseDelay);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        } catch (SocketException e) {
            if (!serverSocket.isClosed()) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void waitForStart() {
        try {
            lock.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRequest(Socket clientSocket, int delay) throws IOException {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String request = in.readLine();
            Thread.sleep(delay);
            out.println(String.format("Echo from server listening on port %d: %s", port, request));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            clientSocket.close();
        }
    }

    public int getResponseDelay() {
        return responseDelay;
    }

    public void setResponseDelay(int responseDelay) {
        this.responseDelay = responseDelay;
    }
}