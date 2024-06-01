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

    private static final byte TURN_LOWER_BOUND = -1, TURN_UPPER_BOUND = 3;

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

        host.clearMoveQueue();
        guest.clearMoveQueue();

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
        turnOrder = guest.turn;

        host.isReady = false;
        guest.isReady = false;
        return true;
    }

    public boolean setTurnOrder(byte turnOrder) {
        if (turnOrder > TURN_LOWER_BOUND && turnOrder < TURN_UPPER_BOUND) {
            this.turnOrder = turnOrder;
            return true;
        }
        return false;
    }

    public boolean setNextOrder(byte nextOrder) {
        if (nextOrder > TURN_LOWER_BOUND && nextOrder < TURN_UPPER_BOUND) {
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
        switch (nextOrder) {
            case ALTERNATING -> turnOrder ^= 1;
            case RANDOM -> turnOrder = new Random().nextInt(2);
        }
        System.out.println("TUrn order" + turnOrder);
    }

    public byte[] getSettings() {
        return new byte[]{(byte) (code >> 8), (byte) code, isPublic, (byte) turnOrder, (byte) nextOrder,
                increment, isUnlimited, (byte) (startTime >> 8), (byte) startTime};
    }

    @Override
    public void close() {
        server.removeLobby(this);
    }
}
