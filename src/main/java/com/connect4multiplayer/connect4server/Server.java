package com.connect4multiplayer.connect4server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Server {
    private static final int INPUT_BYTES = 6;
    private static final int OUTPUT_BYTES = 7;
    private static final byte MOVE = 1;

    private final ArrayList<Game> currentGames = new ArrayList<>();
    private final AsynchronousServerSocketChannel serverSock;
    private final HashMap<AsynchronousSocketChannel, Player> players = new HashMap<>();
    private Optional<Player> waitingPlayer = Optional.empty();

    public Server() throws IOException, ExecutionException, InterruptedException {
        serverSock = AsynchronousServerSocketChannel.open();
        final int PORT = 24454;
        InetSocketAddress addr = new InetSocketAddress(PORT);
        serverSock.bind(addr);
        establishClientConnection();
    }

    private void establishClientConnection() {
        this.serverSock.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Void unused) {
                System.out.println("Connected");
                players.put(client, new Player());
                readFromClient(client);
                establishClientConnection();
            }

            @Override
            public void failed(Throwable throwable, Void unused) {}
        });
    }

    private synchronized void setupGame(AsynchronousSocketChannel client) {
        if (waitingPlayer.isPresent()) {
            int gameId = currentGames.size() - 1;
            waitingPlayer.get().gameId = gameId;
            players.get(client).gameId = gameId;
            currentGames.get(gameId).player2Client = client;
            currentGames.get(gameId).turn = 1;
            System.out.println("found second player");
            waitingPlayer = Optional.empty();
        }
        else {
            Player player = players.get(client);
            player.turn = 1;
            player.gameId = currentGames.size();
            waitingPlayer = Optional.of(player);
            Game game = new Game();
            game.player1Client = client;
            currentGames.add(game);
            System.out.println("found first player");
        }
    }
    
    private void readFromClient(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(INPUT_BYTES);
        client.read(buffer, null, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, Object attachment) {
                try {
                    processClientInput(client, buffer.flip());
                    readFromClient(client);
                }
                catch (BufferUnderflowException e) {
                    try {
                        client.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {}
        });
    }

    private void processClientInput(AsynchronousSocketChannel client, ByteBuffer buffer) {
        System.out.println("processing");
        Player player = players.get(client);
        int type = buffer.get();
        if (type == MOVE) {
            Optional<Move> validMove = currentGames.get(player.gameId).playMove(buffer.get(), player.turn);
            validMove.ifPresent(move -> sendMoveToClients(move, currentGames.get(player.gameId)));
        }
        else {
            System.out.println("player found");
            setupGame(client);
        }
    }

    private void sendMoveToClients(Move move, Game game) {
        System.out.println("sending moves");
        ByteBuffer buffer = ByteBuffer.allocate(OUTPUT_BYTES);
        buffer.put(MOVE);
        buffer.put(move.col());
        buffer.put(move.height());
        buffer.put(move.player());
        byte gameState = game.checkGameOver();
        buffer.put(gameState);
        if(gameState == Game.WIN) {
            for(byte spot: game.getWinningSpots()) {
                buffer.put(spot);
            }
        }
        else {
            buffer.putShort((short) 0);
        }
        buffer.flip();
        game.player1Client.write(buffer);
        buffer.flip();
        game.player2Client.write(buffer);
        System.out.println("sent move to client");
    }

//    public Optional<Move> validateMove(Move move) {
//        currentGames.get(move.gameId()).isValidMove(move.col());
//        return Optional.empty();
//    }

    public CompletableFuture<Optional<Move>> handleMove() {


        CompletableFuture<Optional<Move>> moveFuture = new CompletableFuture<>();

        return moveFuture.whenCompleteAsync((move, throwable) -> {
            if (move.isPresent()) {
                Move m = move.get();
                // pass col back to client
                

            }
        });
    }

//    public void readMove(AsynchronousSocketChannel client, CompletableFuture<Optional<Move>> moveFuture) {
//
//        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + Byte.BYTES);
//        client.read(buffer, null, new CompletionHandler<Integer, Void>() {
//            @Override
//            public void completed(Integer result, Void unused) {
//                // -1 means no more bytes! Close connection
//                // Can also mean that the client disconnected
//                if (result == -1) {
//                    try {
//                        client.close();
//                    } catch (IOException ignored) {
//                        // Do nothing, we don't care if it fails here
//                    }
//
//                    moveFuture.complete(Optional.empty());
//                    return;
//                }
//
//                // If there are still bytes to read, read them
//                buffer.flip();
//                byte col = buffer.get();
//                int gameId = buffer.getInt();
//
//                Move move = new Move(col, gameId);
//
//                Optional<Move> optionalMove = validateMove(move);
//                // Construct our move
//                moveFuture.complete(optionalMove);
//            }
//
//            @Override
//            public void failed(Throwable throwable, Void unused) {
//                moveFuture.complete(Optional.empty());
//            }
//        });
//    }


}
