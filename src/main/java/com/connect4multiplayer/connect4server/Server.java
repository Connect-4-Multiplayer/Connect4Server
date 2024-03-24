package com.connect4multiplayer.connect4server;

import com.connect4multiplayer.connect4server.lobbies.Lobby;
import com.connect4multiplayer.connect4server.messages.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private final AsynchronousServerSocketChannel serverSock;
    private final HashMap<AsynchronousSocketChannel, Player> players = new HashMap<>();

    private static final int INPUT_BYTES = 1024;

    private final ArrayList<Lobby> lobbies = new ArrayList<>();
    private Player waitingPlayer = null;
    public Lobby openLobby = null;

    static int moves;

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

    public void removeLobby(Lobby lobby) {
        lobbies.remove(lobby);
    }

    private void startMatchmaking(AsynchronousSocketChannel client) {
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
        byte reqNum = buffer.get();
        if (Messages.MOVE.hasRequestNum(reqNum)) {
            Message message = Message.of(reqNum);
            message.process(this, player, buffer);
            return;
        }

        if (Messages.FIND_OPPONENT.hasRequestNum(reqNum)){
            startMatchmaking(client);
            return;
        }

        if (Messages.LOBBY_CREATE.hasRequestNum(reqNum)) {
            Lobby lobby = new Lobby(this, player);
            player.lobby = lobby;
            lobbies.add(lobby);
            return;
        }

        if (Messages.LOBBY_JOIN.hasRequestNum(reqNum)) {
            // TODO prevent people from joining after full
            int id = buffer.getInt();
            Lobby lobby = lobbies.get(id);
            lobby.add(player);
            return;
        }

        if (Messages.LOBBY_START.hasRequestNum(reqNum)) {
            player.lobby.startGame();
        }
    }

}
