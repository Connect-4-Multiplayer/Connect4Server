package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class SetReady extends Message {
    public SetReady() {
        this.type = SET_READY;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        player.isReady = true;
    }
}
