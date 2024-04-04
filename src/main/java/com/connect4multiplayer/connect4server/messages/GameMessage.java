package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class GameMessage extends Message {

    private static byte START = 0, RESIGN = 1;

    public GameMessage() {
        type = GAME_MESSAGE;
    }

    public void sendStartGame(AsynchronousSocketChannel hostClient, AsynchronousSocketChannel guestClient) {
        hostClient.write(constructMessage(2, START).flip());
        guestClient.write(constructMessage(2, START).flip());
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {

    }
}
