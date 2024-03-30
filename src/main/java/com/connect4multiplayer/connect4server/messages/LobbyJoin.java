package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Lobby;
import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class LobbyJoin extends Message {

    private static final byte NOT_FOUND = 0, FOUND = 1, CREATED = 2;
    private static final int REPLY_SIZE = 3;
    private static final byte PRIVATE = 0, PUBLIC = 1;

    public LobbyJoin() {
        this.type = LOBBY_JOIN;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        // Reserved values used as markers for creating private or joining public lobbies
        // We use these for padding, to maintain consistency of the size of the messages.
        // The client may also send the private lobby number, which is a short. Having a single byte be sent instead would cause issues
        final short CREATE_PRIVATE = Short.MIN_VALUE;
        final short JOIN_PUBLIC = Short.MAX_VALUE;
        System.out.println("got join");
        short code = buffer.getShort();
        System.out.println("CODE: " + code);
        switch (code) {
            case CREATE_PRIVATE -> {
                player.client.write(constructMessage(4, CREATED)
                        .putShort(server.createPrivateLobby(player)).flip());
                System.out.println("Sent lobby code");
            }
            case JOIN_PUBLIC -> server.joinPublicMatch(player).ifPresentOrElse(this::sendLobbyInfo,
                    () -> sendOpponentNotFoundReply(player, PUBLIC));
            default -> server.joinPrivateLobby(player, code).ifPresentOrElse(this::sendLobbyInfo,
                    () -> sendOpponentNotFoundReply(player, PRIVATE));
        }
    }

    private void sendOpponentNotFoundReply(Player player, byte lobbyType) {
        player.client.write(constructMessage(REPLY_SIZE, NOT_FOUND, lobbyType).flip());
    }

    private void sendLobbyInfo(Lobby lobby) {
        int SIZE = 74;
        lobby.guest.client.write(constructMessage(SIZE, FOUND, lobby.isPublic)
                .put(lobby.getSettings())
                .put(lobby.host.name).flip());
    }
}
