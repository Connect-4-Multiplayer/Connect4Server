package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class LobbyJoin extends Message {

    private final byte FAIL = 0;
    private final byte SUCCESS = 1;
    private final byte PRIVATE = 0;
    private final byte PUBLIC = 1;
    private final int FAILURE_MESSAGE_SIZE = 3;
    private final int SUCCESS_MESSAGE_SIZE = 67;

    public LobbyJoin() {
        this.type = LOBBY_JOIN;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        final short CREATE_PRIVATE = Short.MIN_VALUE;
        final short JOIN_PUBLIC = Short.MAX_VALUE;

        short code = buffer.getShort();
        switch (code) {
            case JOIN_PUBLIC -> {
                ByteBuffer buf = constructMessage(3, PUBLIC);
                server.joinPublicMatch(player).ifPresentOrElse(
                        ,
                        () -> buf.put(FAIL));
                player.client.write(buf.flip());
            }
            case CREATE_PRIVATE -> server.createPrivateLobby(player);
            default -> server.joinPrivateLobby(player, code).ifPresentOrElse(name -> sendJoinPrivateSuccess(player, name), () -> sendJoinPrivateFailure(player));
        }
    }

    private void sendOpponentNotFound(Player player) {
        name -> buf.put(SUCCESS).put(name.getBytes(StandardCharsets.UTF_16BE))
    }

    private void sendOpponentFound(Player player, String hostName) {

    }

    private void sendJoinPrivateFailure(Player player) {
        System.out.println("Failed");
        player.client.write(constructMessage(FAILURE_MESSAGE_SIZE, PRIVATE, FAIL).flip());
    }

    private void sendJoinPrivateSuccess(Player player, String name) {
        System.out.println("Succeeded");
        player.client.write(constructMessage(SUCCESS_MESSAGE_SIZE, PRIVATE, SUCCESS)
                .put(name.getBytes(StandardCharsets.UTF_16BE))
                .flip()
        );
    }
}
