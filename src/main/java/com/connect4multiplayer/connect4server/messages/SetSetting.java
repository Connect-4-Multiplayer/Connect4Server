package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;
import com.connect4multiplayer.connect4server.Lobby;

import java.nio.ByteBuffer;

public class SetSetting extends Message {

    private static final byte TURN_ORDER = 0;
    private static final byte NEXT_ORDER = 1;
    private static final byte START_TIME = 2;
    private static final byte INCREMENT = 3;
    private static final byte IS_UNLIMITED = 4;

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {

        Lobby lobby = player.lobby;

        // Player needs to be in a lobby for this to work
        if (lobby == null) {
            return;
        }

        // If player is not host, do nothing! This player should not
        if (lobby.host != player) {
            return;
        }

        // The setting id. Tells which setting to update
        byte settingId = buffer.get();
        // The option that the setting is becoming. For example, for next order, 1 would be ALTERNATING
        // TODO make sure the option is VALID. Invalid setting values could be dangerous!
        byte settingVal = buffer.get();
        switch (settingId) {
            case TURN_ORDER -> lobby.turnOrder = settingVal;
            case NEXT_ORDER -> lobby.nextOrder = settingVal;
            case START_TIME -> lobby.startTime = (short) ((settingVal << 8) + buffer.get());
            case INCREMENT -> lobby.increment = settingVal;
            case IS_UNLIMITED -> lobby.isUnlimited = settingVal;
        }
    }
}
