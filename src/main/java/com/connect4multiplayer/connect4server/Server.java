package com.connect4multiplayer.connect4server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Server {

    private final ArrayList<Game> currentGames = new ArrayList<>();
    private final AsynchronousServerSocketChannel serverSock;
    public Server(InetAddress ip) throws IOException {
        serverSock = AsynchronousServerSocketChannel.open();

        final int PORT = 24553;
        InetSocketAddress addr = new InetSocketAddress(ip, PORT);

        serverSock.bind(addr);
    }

    public CompletableFuture<Optional<Move>> handleMove() {

        CompletableFuture<Optional<Move>> moveFuture = new CompletableFuture<>();

        this.serverSock.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Void unused) {
                readMove(client, moveFuture);
            }

            @Override
            public void failed(Throwable throwable, Void unused) {
                moveFuture.complete(Optional.empty());
            }
        });

        return moveFuture;
    }

    public void readMove(AsynchronousSocketChannel client, CompletableFuture<Optional<Move>> moveFuture) {

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2);
        client.read(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void unused) {
                // -1 means no more bytes! Close connection
                // Can also mean that the client disconnected
                if (result == -1) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    moveFuture.complete(Optional.empty());
                    return;
                }

                // If there are still bytes to read, read them
                buffer.flip();
                int col = buffer.getInt();
                int gameId = buffer.getInt();

                // Construct our move
                moveFuture.complete(Optional.of(new Move(col, gameId)));
            }

            @Override
            public void failed(Throwable throwable, Void unused) {
                moveFuture.complete(Optional.empty());
            }
        });
    }


}
