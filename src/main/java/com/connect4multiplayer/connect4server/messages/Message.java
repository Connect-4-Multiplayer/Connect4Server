package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public abstract class Message {
    static final byte LOBBY_JOIN = 0;
    static final byte MOVE = 1;
    static final byte PLAYER_INPUT = 2;
    static final byte SET_SETTING = 3;
    static final byte ENGINE_MESSAGE = 4;

    byte type;

    public static Message of(byte type) {
        return switch (type) {
            case LOBBY_JOIN -> new LobbyJoin();
            case MOVE -> new MoveMessage();
            case PLAYER_INPUT -> new PlayerInput();
            case SET_SETTING -> new SetSetting();
            case ENGINE_MESSAGE -> new EngineMessage();
            default -> null;
        };
    }

    /**
     * Creates a reply based on the message
     * @param size The size of the reply to be allocated
     * @param args The bytes to send. Must be smaller than the size of the reply to be constructed
     * @throws IllegalArgumentException when the size is greater than the size of the request
     */
    public ByteBuffer constructMessage(int size, byte... args) {
        return ByteBuffer.allocate(size)
                .put(type)
                .put(args);
    }

    public abstract void process(Server server, Player player, ByteBuffer buffer);
}
