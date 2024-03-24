package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class GameStart extends Message {
    public GameStart(byte type) {
        super(type);
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {

    }
}
