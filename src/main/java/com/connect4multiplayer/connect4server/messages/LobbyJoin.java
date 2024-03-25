package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Lobby;
import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class LobbyJoin extends Message {

    private static final byte NOT_FOUND = 0, FOUND = 1, CREATED = 2;
//    private static final byte SUCCESS = 1;
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

        short code = buffer.getShort();
        switch (code) {
            case JOIN_PUBLIC -> {
                server.joinPublicMatch(player).ifPresentOrElse(this::sendSettingsToGuest,
                        () -> sendReply(player, NOT_FOUND, PUBLIC));
            }
            case CREATE_PRIVATE -> {
                server.createPrivateLobby(player);
                sendReply(player, CREATED, PRIVATE);
            }
            default -> server.joinPrivateLobby(player, code).ifPresentOrElse(this::sendSettingsToGuest,
                    () -> sendReply(player, NOT_FOUND, PRIVATE));
        }
    }

    private void sendReply(Player player, byte code, byte lobbyType) {
        player.client.write(constructMessage(REPLY_SIZE, code, lobbyType).flip());
    }

    private void sendSettingsToGuest(Lobby lobby) {
        int SETTINGS_SIZE = 74;
        lobby.guest.client.write(constructMessage(SETTINGS_SIZE, FOUND, lobby.isPublic)
                .put(lobby.getSettings())
                .put(lobby.host.name).flip());
    }
}
