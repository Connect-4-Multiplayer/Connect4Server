package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class PlayerSelection extends Message {
    final byte NAME = 0, SET_READY = 1;

    public PlayerSelection() {
        type = PLAYER_SELECTION;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        byte selection = buffer.get();
        switch (selection) {
            case NAME -> {
                byte[] name = new byte[64];
                int i = 0;
                while (buffer.hasRemaining()) name[i++] = buffer.get();
                player.name = name;
            }
            case SET_READY -> player.isReady = true;
        }
    }
}
