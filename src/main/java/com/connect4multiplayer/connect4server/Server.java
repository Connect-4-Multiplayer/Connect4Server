package com.connect4multiplayer.connect4server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Server {

    public static void main(String[] args) {
        ByteBuffer buff = ByteBuffer.allocate(2);
        buff.put((byte) 2);
        System.out.println(buff);
        System.out.println(buff.put((byte) 4) == buff);
        System.out.println(buff);
    }
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

        CompletableFuture<AsynchronousSocketChannel> future = new CompletableFuture<>();
        this.serverSock.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Void unused) {
                System.out.println("Connected");
                players.put(client, new Player(client));
                readFromClient(client);
                establishClientConnection();
                future.complete(client);
            }

            @Override
            public void failed(Throwable throwable, Void unused) {}
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
    
    private void readFromClient(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(INPUT_BYTES);
        client.read(buffer, null, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, Object attachment) {
                processClientInput(client, buffer.flip());
                // Accept another read
                readFromClient(client);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {}
        });
    }

    private void processClientInput(AsynchronousSocketChannel client, ByteBuffer buffer) {
        Player player = players.get(client);
        System.out.println(player.game);
        int type = buffer.get();
        if (Message.MOVE.isType(type)) {
            Optional<Move> validMove = player.game.playMove(buffer.get(), (byte) player.turn);
            validMove.ifPresentOrElse(move -> {
                sendMoveToClients(move, player.game);
            }, () -> {

            });
        }
        else {
            setupGame(client);
        }
    }

    private void sendMoveToClients(Move move, Game game) {
        ByteBuffer buffer = Message.MOVE.constructReply(move.col(), move.height(), move.player());
        byte gameState = game.getGameState();
        if (gameState != Game.WIN) {
            buffer.put(gameState);
            buffer.putShort((short) 0);
            game.player1.client.write(buffer.flip());
            game.player2.client.write(buffer.flip());
        }
        else {
            byte winner = (byte) game.turn;
            buffer.put(winner);
            buffer.put(game.getWinningSpots());
            game.player1.client.write(buffer.flip());
            // Flips winner bit since the other player has a victory when the first has a loss and vice versa
            buffer.put(4, (byte) (winner ^ 1));
            game.player2.client.write(buffer.flip());
        }
    }

}
