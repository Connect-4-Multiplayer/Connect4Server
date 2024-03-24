package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class MatchMaking extends Message {
    public MatchMaking(byte type) {
        super(type);
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        
    }
}
