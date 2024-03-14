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

    public static void main(String[] args) {
        ByteBuffer buff = ByteBuffer.allocate(2);
        buff.put((byte) 2);
        System.out.println(buff);
        System.out.println(buff.put((byte) 4) == buff);
        System.out.println(buff);
    }
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
            Player opponent = waitingPlayer.get();
            opponent.gameId = gameId;
            Game game = currentGames.get(gameId);
            Player player = players.get(client);
            player.gameId = gameId;
            player.game = game;
            game.player2Client = client;
            game.turn = 1;
            System.out.println("found second player");
            waitingPlayer = Optional.empty();
        }
        else {
            Game game = new Game();
            game.player1Client = client;
            Player player = players.get(client);
            player.turn = 1;
            player.gameId = currentGames.size();
            player.game = game;
            waitingPlayer = Optional.of(player);
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
        Player player = players.get(client);
        int type = buffer.get();
        if (type == MOVE) {
            Optional<Move> validMove = player.game.playMove(buffer.get(), player.turn);
            validMove.ifPresent(move -> sendMoveToClients(move, currentGames.get(player.gameId)));
        }
        else {
            setupGame(client);
        }
    }

    private void sendMoveToClients(Move move, Game game) {
        ByteBuffer buffer = ByteBuffer.allocate(OUTPUT_BYTES);
        buffer.put(MOVE);
        buffer.put(move.col());
        buffer.put(move.height());
        buffer.put(move.player());
        byte gameState = game.getGameState();
        if (gameState != Game.WIN) {
            buffer.put(gameState);
            buffer.putShort((short) 0);
            game.player1Client.write(buffer.flip());
            game.player2Client.write(buffer.flip());
        }
        else {
            byte winner = (byte) game.turn;
            buffer.put(winner);
            for (byte spot: game.getWinningSpots()) buffer.put(spot);
            game.player1Client.write(buffer.flip());
            buffer.put(4, (byte) (winner ^ 1));
            game.player2Client.write(buffer.flip());
        }
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

//                Move move = new Move(col, gameId);

//                Optional<Move> optionalMove = validateMove(move);
                // Construct our move
//                moveFuture.complete(optionalMove);
            }

            @Override
            public void failed(Throwable throwable, Void unused) {
                moveFuture.complete(Optional.empty());
            }
        });
    }


}
