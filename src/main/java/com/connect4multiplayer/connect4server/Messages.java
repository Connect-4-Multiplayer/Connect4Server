package com.connect4multiplayer.connect4server;

import java.nio.ByteBuffer;

public enum Messages {

    FIND_OPPONENT(0),
    MOVE(1),
    LOBBY_CREATE(2),
    LOBBY_JOIN(3),
    LOBBY_START(4);

    final byte type;

    Messages(int type) {
        this.type = (byte) type;
    }

    public boolean hasRequestNum(int type) {
        return this.type == type;
    }

    /**
     * Sends a {@link messages} based on the constant selected
     * @param args The bytes to send. Must be smaller than the size of the request used
     * @throws IllegalArgumentException when the size is greater than the size of the request
     */
    public void sendReply(Player player, byte... args) {
        player.client.write(ByteBuffer.allocate(args.length + 1)
                .put(type)
                .put(args)
                .flip()
        );
    }

    public ByteBuffer constructReply(int size, byte... args) {
        return ByteBuffer.allocate(size)
                .put(type)
                .put(args);
    }

    public Messages getType(byte type) {
        switch (type) {
            case 0: return FIND_OPPONENT;
            case 1: return MOVE;
            case 2: return LOBBY_CREATE;
            case 3: return LOBBY_JOIN;
            case 4: return LOBBY_START;
        }
        return null;
    }
}
