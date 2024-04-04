package com.connect4multiplayer.connect4server;

import java.io.Closeable;
import java.io.IOException;
import java.util.Random;

public class Lobby implements Closeable {

    // Turn orders
    private static final byte HOST = 0;
    private static final byte GUEST = 1;
    private static final byte INIT_RANDOM = 2;

    // Next orders
    private static final byte ALTERNATING = 0;
    private static final byte RANDOM = 1;
//    private static final byte STAY = 2;

    public Server server;
    public Game game;
    public Player host;
    public Player guest;
    public short code;
    
    public final byte isPublic;
    public int turnOrder = INIT_RANDOM;
    public int nextOrder = ALTERNATING;
    public short startTime = 180;
    public byte increment = 0;
    public byte isUnlimited = 0;

    public Lobby(Server server, Player host, short code, boolean isPublic) {
        this.server = server;
        this.code = code;
        this.isPublic = (byte) (isPublic ? 1 : 0);
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

    public boolean startGame() {
        // Don't start if there is not enough players
        if (host == null || guest == null || !host.isReady || !guest.isReady) return false;

        game = new Game(host, guest);
        host.game = game;
        guest.game = game;

        host.moves.clear();
        guest.moves.clear();

        Random rand = new Random();
        switch (turnOrder) {
            case HOST -> host.turn = 1;
            case GUEST -> host.turn = 0;
            case INIT_RANDOM -> host.turn = rand.nextInt(2);
        }
        guest.turn = host.turn ^ 1;

        host.isReady = false;
        guest.isReady = false;
        return true;
    }

    public boolean setTurnOrder(byte turnOrder) {
        if (turnOrder > -1 && turnOrder < 3) {
            this.turnOrder = turnOrder;
            return true;
        }
        return false;
    }

    public boolean setNextOrder(byte nextOrder) {
        if (nextOrder > -1 && nextOrder < 3) {
            this.nextOrder = nextOrder;
            return true;
        }
        return false;
    }

    public boolean setStartTime(short startTime) {
        if (startTime > 0) {
            this.startTime = startTime;
            return true;
        }
        return false;
    }

    public boolean setIncrement(byte increment) {
        if (increment >= 0) {
            this.increment = increment;
            return true;
        }
        return false;
    }

    public boolean setUnlimitedTime(byte isUnlimited) {
        this.isUnlimited = (byte) (isUnlimited == 0 ? 0 : 1);
        return true;
    }

    public void updateSettingsAfterGame() {
        Random rand = new Random();
        switch (nextOrder) {
            case ALTERNATING -> turnOrder ^= 1;
            case RANDOM -> turnOrder = rand.nextInt(2) ;
        }
    }

    public byte[] getSettings() {
        return new byte[]{isPublic, (byte) turnOrder, (byte) nextOrder,
                isUnlimited, increment, (byte) (startTime >> 8), (byte) startTime};
    }

    @Override
    public void close() throws IOException {
        server.removeLobby(this);
    }
}
