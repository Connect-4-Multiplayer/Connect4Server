package com.connect4multiplayer.connect4server;

import java.util.Random;

public class Lobby {

    Game game;
    Player hostPlayer, joiningPlayer;

    // Turn orders
    private static final byte HOST = 0;
    private static final byte JOINING = 1;
    private static final byte INIT_RANDOM = 2;

    // Next orders
    private static final byte STAY = 0;
    private static final byte ALTERNATING = 1;
    private static final byte RANDOM = 2;


    int turnOrder = INIT_RANDOM;

    int nextOrder = ALTERNATING;
    TimeSetting timeSetting = new TimeSetting(false, 180, 0);

    public Lobby(Player host) {
        hostPlayer = host;
        System.out.println("found first player");
    }


    public void join(Player player) {
        joiningPlayer = player;
    }

    public void startGame() {
        game = new Game();
        hostPlayer.game = game;
        joiningPlayer.game = game;


        Random rand = new Random();
        switch (turnOrder) {
            case HOST -> hostPlayer.turn = 1;
            case JOINING -> hostPlayer.turn = 0;
            case INIT_RANDOM -> hostPlayer.turn = rand.nextInt(2);
        }

        game.turn = 1;
    }

    public void updateSettingsAfterGame() {

        Random rand = new Random();
        switch (nextOrder) {
            case ALTERNATING -> turnOrder = turnOrder ^ 1;
            case RANDOM -> turnOrder = rand.nextInt(2) ;
        }

        joiningPlayer.turn = hostPlayer.turn ^ 1;
    }

}
