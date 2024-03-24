package com.connect4multiplayer.connect4server.lobbies;

import com.connect4multiplayer.connect4server.Game;
import com.connect4multiplayer.connect4server.Server;
import com.connect4multiplayer.connect4server.Player;

import java.io.Closeable;
import java.io.IOException;
import java.util.Random;

public class Lobby implements Closeable {

    // Turn orders
    private static final byte HOST = 0;
    private static final byte JOINING = 1;
    private static final byte INIT_RANDOM = 2;

    // Next orders
    private static final byte STAY = 0;
    private static final byte ALTERNATING = 1;
    private static final byte RANDOM = 2;


    Server server;
    Game game;
    Player host, guest;
    int turnOrder = INIT_RANDOM;
    int nextOrder = ALTERNATING;
    TimeSetting timeSetting = new TimeSetting(false, 180, 0);


    public Lobby(Server server, Player host) {
        this.server = server;
        add(host);
        System.out.println("found first player");
    }


    public void add(Player player) {
        if (host == null) {
            host = player;
            player.lobby = this;
        } else if (guest == null) {
            guest = player;
            player.lobby = this;
        }

        // If both aren't null, the player cannot join!
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

        game = new Game();

        host.game = game;
        guest.game = game;

        Random rand = new Random();
        switch (turnOrder) {
            case HOST -> host.turn = 1;
            case JOINING -> host.turn = 0;
            case INIT_RANDOM -> host.turn = rand.nextInt(2);
        }

        guest.turn = host.turn ^ 1;

        game.turn = 1;
    }

    public void updateSettingsAfterGame() {

        Random rand = new Random();
        switch (nextOrder) {
            case ALTERNATING -> turnOrder = turnOrder ^ 1;
            case RANDOM -> turnOrder = rand.nextInt(2) ;
        }
    }

    @Override
    public void close() throws IOException {
        server.removeLobby(this);
    }
}
