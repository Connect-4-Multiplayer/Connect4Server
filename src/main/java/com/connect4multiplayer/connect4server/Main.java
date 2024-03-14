package com.connect4multiplayer.connect4server;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Hello world!
 *
 */
public class Main {
    private static final Object waitForever = new Object();
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        new Server();
        synchronized (waitForever) { waitForever.wait(); }
    }
}
