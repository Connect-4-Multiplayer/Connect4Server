package com.connect4multiplayer.connect4server;

import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Server {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        new Server();
        synchronized (waitForever) { waitForever.wait(); }
    }

    private final ArrayList<Game> currentGames = new ArrayList<>();
    private final AsynchronousServerSocketChannel serverSock;
    private static final Object waitForever = new Object();

    public Server() throws IOException, ExecutionException, InterruptedException {
        serverSock = AsynchronousServerSocketChannel.open();
        final int PORT = 24554;
        InetSocketAddress addr = new InetSocketAddress(PORT);
        serverSock.bind(addr);
        establishClientConnection();
    }

    private void establishClientConnection() {
        this.serverSock.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Void unused) {
                System.out.println("Connected");
                readFromClient(client);
                establishClientConnection();
            }

            @Override
            public void failed(Throwable throwable, Void unused) {}
        });
    }
    
    private void readFromClient(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        client.read(buffer, null, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, Object attachment) {
                buffer.flip();
                client.write(buffer);
                readFromClient(client);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
            }
        });
    }

    public Optional<Move> validateMove(Move move) {
        // TODO
        return Optional.empty();
    }

    public CompletableFuture<Optional<Move>> handleMove() {
        

        CompletableFuture<Optional<Move>> moveFuture = new CompletableFuture<>();

        return moveFuture.whenCompleteAsync((move, throwable) -> {
            if (move.isPresent()) {
                Move m = move.get();
                // pass col back to client
                

            }
        });
    }

    public void readMove(AsynchronousSocketChannel client, CompletableFuture<Optional<Move>> moveFuture) {

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + Byte.BYTES);
        client.read(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void unused) {
                // -1 means no more bytes! Close connection
                // Can also mean that the client disconnected
                if (result == -1) {
                    try {
                        client.close();
                    } catch (IOException ignored) {
                        // Do nothing, we don't care if it fails here
                    }

                    moveFuture.complete(Optional.empty());
                    return;
                }

                // If there are still bytes to read, read them
                buffer.flip();
                byte col = buffer.get();
                int gameId = buffer.getInt();

                Move move = new Move(col, gameId);

                Optional<Move> optionalMove = validateMove(move);
                // Construct our move
                moveFuture.complete(optionalMove);
            }

            @Override
            public void failed(Throwable throwable, Void unused) {
                moveFuture.complete(Optional.empty());
            }
        });
    }


}
