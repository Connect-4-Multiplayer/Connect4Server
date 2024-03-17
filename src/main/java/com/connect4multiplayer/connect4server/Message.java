package com.connect4multiplayer.connect4server;

import java.nio.ByteBuffer;

public enum Message {

    FIND_OPPONENT(0),
    MOVE(1),
    LOBBY(2);

    final byte type;

    Message(int type) {
        this.type = (byte) type;
    }

    public boolean isType(int type) {
        return this.type == type;
    }

    /**
     * Sends a {@link Message} based on the constant selected
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

    public ByteBuffer constructReply(byte... args) {
        return ByteBuffer.allocate(args.length + 1)
                .put(type)
                .put(args);
    }

}
