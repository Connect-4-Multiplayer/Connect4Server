package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;
import com.connect4multiplayer.connect4server.Lobby;

import java.nio.ByteBuffer;

public class SetSetting extends Message {

    static final byte TURN_ORDER = 0;
    private static final byte NEXT_ORDER = 1;
    private static final byte START_TIME = 2;
    private static final byte INCREMENT = 3;
    private static final byte IS_UNLIMITED = 4;

    public SetSetting() {
        type = SET_SETTING;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        Lobby lobby = player.lobby;

        // Player needs to be in a lobby for this to work
        if (lobby == null) return;

        // If player is not host, do nothing! This player should not
        if (lobby.host != player) return;

        // The setting id. Tells which setting to update
        byte settingId = buffer.get();

        // The option that the setting is becoming. For example, for next order, 1 would be ALTERNATING
        byte settingVal0 = buffer.get();
        byte settingVal1 = 0;
        boolean settingsChanged = switch (settingId) {
            case TURN_ORDER -> lobby.setTurnOrder(settingVal0);
            case NEXT_ORDER -> lobby.setNextOrder(settingVal0);
            case START_TIME -> {
                settingVal1 = buffer.get();
                yield lobby.setStartTime((short) (((settingVal0 & 255) << 8) + (settingVal1 & 255)));
            }
            case INCREMENT -> lobby.setIncrement(settingVal0);
            case IS_UNLIMITED -> lobby.setUnlimitedTime(settingVal0);
            default -> false;
        };
        if (settingsChanged) sendSetting(lobby, settingId, settingVal0, settingVal1);
    }

    public void sendSetting(Lobby lobby, byte settingId, byte settingVal0, byte settingVal1) {
        final int SETTING_SIZE = 4;
        ByteBuffer settingsMessage = constructMessage(SETTING_SIZE, settingId, settingVal0, settingVal1);
        lobby.host.client.write(settingsMessage.flip());
        lobby.guest.client.write(settingsMessage.flip());
    }
}
