package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class LobbyJoin extends Message {

    private static final byte FAIL = 0;
    private static final byte SUCCESS = 1;
    private static final int FAILURE_MESSAGE_SIZE = 2;
    private static final int SUCCESS_MESSAGE_SIZE = 66;

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
            case JOIN_PUBLIC -> server.joinPublicMatch(player);
            case CREATE_PRIVATE -> server.createPrivateLobby(player);
            default -> server.joinPrivateLobby(player, code).ifPresentOrElse(name -> sendSuccess(player, name), () -> sendFailure(player));
        }
    }

    private void sendFailure(Player player) {
        player.client.write(constructMessage(FAILURE_MESSAGE_SIZE, LOBBY_JOIN, FAIL).flip());
    }

    private void sendSuccess(Player player, String name) {
        player.client.write(constructMessage(SUCCESS_MESSAGE_SIZE, LOBBY_JOIN, SUCCESS)
                .put(name.getBytes(StandardCharsets.UTF_16BE))
                .flip()
        );
    }
}
