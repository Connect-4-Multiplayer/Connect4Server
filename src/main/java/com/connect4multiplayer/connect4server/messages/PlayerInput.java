package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Lobby;
import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class PlayerInput extends Message {
    static final byte NAME = 0, TOGGLE_READY = 1;
    static final int MAX_NAME_LENGTH = 64;

    public PlayerInput() {
        type = PLAYER_INPUT;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        byte selection = buffer.get();
        switch (selection) {
            case NAME -> player.getOpponent().client.write(constructMessage(MAX_NAME_LENGTH + 2, NAME).put(buffer).flip());
            case TOGGLE_READY -> {
                player.isReady = !player.isReady;
                Lobby lobby = player.lobby;
                lobby.host.client.write(constructMessage(3, TOGGLE_READY, (byte) (player.isHost ? 0 : 1)).flip());
                lobby.guest.client.write(constructMessage(3, TOGGLE_READY, (byte) (player.isHost ? 0 : 1)).flip());
                lobby.startGame();
            }
        }
    }
}
