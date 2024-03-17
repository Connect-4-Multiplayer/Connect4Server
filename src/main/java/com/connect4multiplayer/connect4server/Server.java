package com.connect4multiplayer.connect4server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Server {

    private static final int INPUT_BYTES = 6;
    private final AsynchronousServerSocketChannel serverSock;
    private final HashMap<AsynchronousSocketChannel, Player> players = new HashMap<>();
    private Player waitingPlayer = null;

    public Server() throws IOException {
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
                players.put(client, new Player(client));
                readFromClient(client);
                establishClientConnection();
            }

            @Override
            public void failed(Throwable throwable, Void unused) {
                System.out.println("failed");
            }
        });

    }

    private void setupGame(AsynchronousSocketChannel client) {
        if (waitingPlayer != null) {
            Player opponent = waitingPlayer;
            Game game = opponent.game;
            Player player = players.get(client);
            player.turn = 0;
            player.game = game;
            game.player2 = player;
            game.turn = 1;
            System.out.println("found second player");
            waitingPlayer = null;
        }
        else {
            Game game = new Game();
            game.player1 = players.get(client);
            game.player1.game = game;
            waitingPlayer = game.player1;
            System.out.println("found first player");
        }
    }
    
    private synchronized void readFromClient(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(INPUT_BYTES);
        client.read(buffer, null, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, Object attachment) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    processClientInput(client, buffer);
                }
                // Accept another read
                readFromClient(client);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {}
        });
    }

    private synchronized void processClientInput(AsynchronousSocketChannel client, ByteBuffer buffer) {
        Player player = players.get(client);
        int type = buffer.get();
        if (Message.MOVE.isType(type)) {
            player.enqueueMove(buffer.get());
            handleMove(player);
        }
        else {
            setupGame(client);
        }
    }

    static int moves;

    private void handleMove(Player player) {
        player.playMove().ifPresent(move -> {
            sendMoveToClients(move, player.game);
            handleMove(player.getOpponent());
        });
    }

    private CompletableFuture<Move> sendMoveToClients(Move move, Game game) {
        moves++;
        CompletableFuture<Move> moveFuture = new CompletableFuture<>();
        ByteBuffer buffer = Message.MOVE.constructReply(7, move.col(), move.height(), move.player());
        ByteBuffer buffer1 = Message.MOVE.constructReply(7, move.col(), move.height(), move.player());
        byte gameState = game.getGameState();
        if (gameState != Game.WIN) {
            buffer.put(gameState).putShort((short) 0);
            buffer1.put(gameState).putShort((short) 0);
            game.player1.client.write(buffer.flip());
            game.player2.client.write(buffer1.flip());
        }
        else {
            byte winner = (byte) game.turn;
            buffer.put(winner).put(game.getWinningSpots());
            game.player1.client.write(buffer.flip());
            // Flips winner bit since the other player has a victory when the first has a loss and vice versa
            buffer.put(4, (byte) (winner ^ 1));
            game.player2.client.write(buffer.flip());
        }
        return moveFuture;
    }

}
