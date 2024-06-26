package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Lobby;
import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PlayerInput extends Message {
    static final byte CHANGE_NAME = 0, TOGGLE_READY = 1, QUIT = 2;
    static final int MAX_NAME_LENGTH = 64;

    public PlayerInput() {
        type = PLAYER_INPUT;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        byte selection = buffer.get();
        switch (selection) {
            case CHANGE_NAME -> {
                player.name = new byte[buffer.remaining()];
                int i = 0;
                while(buffer.hasRemaining()) {
                    player.name[i++] = buffer.get();
                }
                player.getOpponent().client.write(constructMessage(MAX_NAME_LENGTH + 2, CHANGE_NAME).put(player.name).flip());
            }
            case TOGGLE_READY -> {
                player.isReady = !player.isReady;
                Lobby lobby = player.lobby;
                byte playerRole = (byte) (player.lobby.host == player ? 0 : 1);
                lobby.host.client.write(constructMessage(3, TOGGLE_READY, playerRole).flip());
                if (lobby.guest != null) {
                    lobby.guest.client.write(constructMessage(3, TOGGLE_READY, playerRole).flip());
                }
                if (lobby.startGame()) {
                    new GameMessage().sendStartGame(lobby);
                }
            }
            case QUIT -> {
                try {
                    if(player.getOpponent() != null) {
                        player.getOpponent().client.write(constructMessage(2, QUIT).flip());

                    }
                    player.lobby.remove(player);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
