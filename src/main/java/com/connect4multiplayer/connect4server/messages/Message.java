package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public abstract class Message {
    static final byte LOBBY_JOIN = 0;
    static final byte MOVE = 1;
    static final byte SET_READY = 2;

    byte type;

    public static Message of(byte type) {
        return switch (type) {
            case LOBBY_JOIN -> new LobbyJoin();
            case MOVE -> new MoveMessage();
            case SET_READY -> new SetReady();
            default -> null;
        };
    }

    public ByteBuffer constructReply(int size, byte... args) {
        return ByteBuffer.allocate(size)
                .put(type)
                .put(args);
    }

    public abstract void process(Server server, Player player, ByteBuffer buffer);
}
