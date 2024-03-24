package com.connect4multiplayer.connect4server.lobbies;

import com.connect4multiplayer.connect4server.Game;
import com.connect4multiplayer.connect4server.Server;
import com.connect4multiplayer.connect4server.Player;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Lobby implements Closeable {

    // Turn orders
    private static final byte HOST = 0;
    private static final byte GUEST = 1;
    private static final byte INIT_RANDOM = 2;

    // Next orders
    private static final byte STAY = 0;
    private static final byte ALTERNATING = 1;
    private static final byte RANDOM = 2;


    public Server server;
    public Game game;
    public Player host;
    public Player guest;
    public short code;
    
    public final byte isPrivate;
    public int turnOrder = INIT_RANDOM;
    public int nextOrder = ALTERNATING;
    short startTime = 180;
    byte increment = 0;
    byte isUnlimited = 0;

    public Lobby(Server server, Player host, short code, boolean isPrivate) {
        this.server = server;
        this.code = code;
        this.isPrivate = (byte) (isPrivate ? 1 : 0);
        add(host);
    }

    public boolean add(Player player) {
        if (host == null) {
            host = player;
            player.lobby = this;
            player.isHost = true;
            return true;
        } else if (guest == null) {
            guest = player;
            player.lobby = this;
            return true;
        }

        // If both aren't null, the player cannot join!
        return false;
    }

    public void remove(Player player) throws IOException {
        if (host.equals(player)) {
            host = guest;
        }
        guest = null;
        // Lobby is disbanded if nobody is in it
        if (host == null) {
            this.close();
        }
    }

    public void startGame() {
        // Don't start if there is not enough players
        if (host == null || guest == null || !host.isReady || !guest.isReady) return;

        game = new Game(host, guest);
        host.game = game;
        guest.game = game;

        Random rand = new Random();
        switch (turnOrder) {
            case HOST -> host.turn = 1;
            case GUEST -> host.turn = 0;
            case INIT_RANDOM -> host.turn = rand.nextInt(2);
        }
        guest.turn = host.turn ^ 1;

        host.isReady = false;
        guest.isReady = false;
    }

    public void updateSettingsAfterGame() {
        Random rand = new Random();
        switch (nextOrder) {
            case ALTERNATING -> turnOrder ^= 1;
            case RANDOM -> turnOrder = rand.nextInt(2) ;
        }
    }

    @Override
    public void close() throws IOException {
        server.removeLobby(this);
    }

    public byte[] getSettings() {
        final int size = 134;
        byte[] settings = new byte[size];
        int i = 0;
        for (byte b : new byte[]{isPrivate, (byte) turnOrder, (byte) nextOrder,
                isUnlimited, increment, (byte) (startTime >> 8), (byte) startTime}) {
            settings[i++] = b;
        }
        for (byte b : host.name.getBytes(StandardCharsets.UTF_16BE)) settings[i++] = b;
        for (byte b : guest.name.getBytes(StandardCharsets.UTF_16BE)) settings[i++] = b;
        return settings;
    }
    
    public void setSettings(byte turnOrder, byte nextOrder, byte isUnlimited, byte increment, short startTime) {
        this.turnOrder = turnOrder;
        this.nextOrder = nextOrder;
        this.isUnlimited = isUnlimited;
        this.increment = increment;
        this.startTime = startTime;
    }
}
