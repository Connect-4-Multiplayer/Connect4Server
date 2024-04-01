package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class EngineMessage extends Message {

    private static final byte PREV_MOVE = 0, NEXT_MOVE = 1, SELECT_MOVE;

    public EngineMessage() {
        type = ENGINE_MESSAGE;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {

    }
}
