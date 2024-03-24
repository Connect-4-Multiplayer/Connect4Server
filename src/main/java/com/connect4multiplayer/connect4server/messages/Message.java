package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public abstract class Message {
    static final byte MATCH_MAKING = 0;
    static final byte MOVE = 1;
    static final byte LOBBY_JOIN = 2;
    static final byte GAME_START = 3;

    byte type;

    public Message(byte type) {
        this.type = type;
    }

    public static Message of(byte type) {
        return switch (type) {
            case MATCH_MAKING -> new MatchMaking(type);
            case MOVE -> new Move(type);
            case LOBBY_JOIN -> new LobbyJoin(type);
            default -> new GameStart(type);
        };
    }

    public ByteBuffer constructReply(int size, byte... args) {
        return ByteBuffer.allocate(size)
                .put(type)
                .put(args);
    }

    public abstract void process(Server server, Player player, ByteBuffer buffer);
}
