package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class PlayerSelection extends Message {
    static final byte NAME = 0, SET_READY = 1;
    static final int MAX_NAME_LENGTH = 64;

    public PlayerSelection() {
        type = PLAYER_SELECTION;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        System.out.println("Player selection");
        byte selection = buffer.get();
        switch (selection) {
            case NAME -> {
                // TODO: fix buffer underflow
                byte[] name = new byte[MAX_NAME_LENGTH];
                buffer.get(name);
                player.name = name;
                System.out.println("Player selection");
                player.getOpponent().client.write(constructMessage(NAME).put(name).flip());
            }
            case SET_READY -> {
                player.isReady = true;
                player.lobby.startGame();
            }
        }
    }
}
