package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Lobby;
import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class GameMessage extends Message {

    private static byte START = 0, RESIGN = 1;

    public GameMessage() {
        type = GAME_MESSAGE;
    }

    public void sendStartGame(Lobby lobby) {
        final int START_MESSAGE_SIZE = 3;
        ByteBuffer message = constructMessage(START_MESSAGE_SIZE, START, (byte) lobby.host.turn);
        lobby.host.client.write(message.flip());
        lobby.guest.client.write(message.flip());
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {

    }
}
