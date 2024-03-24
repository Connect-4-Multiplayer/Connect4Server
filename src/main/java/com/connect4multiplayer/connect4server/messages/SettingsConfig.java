package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;
import com.connect4multiplayer.connect4server.lobbies.Lobby;

import java.nio.ByteBuffer;

public class SettingsConfig extends Message{

    public final byte HOST = 0, GUEST = 1;
    public

    public SettingsConfig(byte type) {
        this.type = type;
    }

    public void sendSettingsToPlayers(Lobby lobby) {
        ByteBuffer buffer = constructMessage(135, lobby.getSettings()).put(HOST);
        lobby.host.client.write(buffer.flip());
        buffer.put(134, GUEST);
        lobby.guest.client.write(buffer.flip());
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        if (player.isHost) player.lobby.setSettings(buffer.get(), buffer.get(), 
                buffer.get(), buffer.get(), buffer.getShort());
    }
}
