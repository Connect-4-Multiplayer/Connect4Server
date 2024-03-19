package com.connect4multiplayer.connect4server;

import java.util.Random;

public class Lobby {

    Game game;
    Player hostPlayer, joiningPlayer;


    enum InitialOrder {
        HOST,
        JOINING,
        RANDOM
    }
    enum NextOrder {
        STAY,
        RANDOM,
        ALTERNATING
    }

    InitialOrder turnOrder = InitialOrder.RANDOM;

    NextOrder nextOrder = NextOrder.ALTERNATING;
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
        hostPlayer.turn = 0;

        //TODO
    }

    public void updateSettingsAfterGame(Game prevGame) {
        Random rand = new Random();
        switch (turnOrder) {
            case HOST -> prevGame.turn = 1;
            case JOINING -> prevGame.turn = 0;
            case RANDOM -> prevGame.turn = rand.nextInt();
        }

        switch (nextOrder) {
            case RANDOM -> prevGame.turn = rand.nextInt();
            case ALTERNATING -> prevGame.turn ^= 1;
        }
    }

//    private void setupGame(AsynchronousSocketChannel client) {
//
//        game = new Game();
//        Player opponent = hostPlayer;
//        Game game = opponent.game;
//        Player player = players.get(client);
//        player.turn = 0;
//        player.game = game;
//        game.player2 = player;
//        game.turn = 1;
//        System.out.println("found second player");
//        hostPlayer = null;
//    }


}
