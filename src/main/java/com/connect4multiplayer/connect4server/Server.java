package com.connect4multiplayer.connect4server;

import com.connect4multiplayer.connect4server.lobbies.Lobby;
import com.connect4multiplayer.connect4server.messages.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private final AsynchronousServerSocketChannel serverSock;
    private final ConcurrentHashMap<AsynchronousSocketChannel, Player> players = new ConcurrentHashMap<>();

    private static final int INPUT_BYTES = 1024;

    public final ConcurrentHashMap<Short, Lobby> lobbies = new ConcurrentHashMap<>();
    private final Set<Short> availableCodes = Collections.synchronizedSet(new HashSet<>());
    private Lobby openLobby = null;

    public Server() throws IOException {
        serverSock = AsynchronousServerSocketChannel.open();
        final int PORT = 24454;
        InetSocketAddress addr = new InetSocketAddress(PORT);
        serverSock.bind(addr);
        establishClientConnection();
        for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
            availableCodes.add(i);
        }
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

    private synchronized void readFromClient(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(INPUT_BYTES);
        client.read(buffer, null, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, Object attachment) {
                try {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        processClientInput(client, buffer);
                    }
                    // Accept another read
                    readFromClient(client);
                }
                catch (Exception e) {
                    try {
                        System.out.println("Closed");
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

    private synchronized void processClientInput(AsynchronousSocketChannel client, ByteBuffer buffer) {
        Message.of(buffer.get()).process(this, players.get(client), buffer);
    }

    public void createPrivateLobby(Player player) {
        short code = getNextCode();
        lobbies.put(code, new Lobby(this, player, code, true));
    }

    public synchronized Optional<String> joinPrivateLobby(Player player, short code) {
        Lobby lobby = lobbies.get(code);
        if (lobby == null || !lobby.add(player)) return Optional.empty();
        return Optional.of(lobby.host.name);
    }

    public synchronized Optional<String> joinPublicMatch(Player player) {
        if (openLobby == null) {
            short code = availableCodes.iterator().next();
            availableCodes.remove(code);
            openLobby = new Lobby(this, player, code, false);
            player.lobby = openLobby;
            player.isReady = true;
            System.out.println("found first player");
            return Optional.empty();
        }
        else {
            Lobby lobby = player.lobby = openLobby;
            player.isReady = true;
            openLobby.add(player);
            openLobby.startGame();
            lobbies.put(openLobby.code, openLobby);
            openLobby = null;
            System.out.println("found second player");
            return Optional.of(lobby.host.name);
        }
    }

    private short getNextCode() {
        short code = availableCodes.iterator().next();
        availableCodes.remove(code);
        return code;
    }

    public synchronized void removeLobby(Lobby lobby) {
        lobbies.remove(lobby.code);
        availableCodes.add(lobby.code);
    }

}
