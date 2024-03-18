package com.connect4multiplayer.connect4server;

import java.nio.channels.AsynchronousSocketChannel;

public class Lobby {

    Game game;
    Player hostPlayer, joiningPlayer;

    enum TurnOrder {
        FIRST,
        SECOND,
        RANDOM,
        ALTERNATING
    }

    TurnOrder turnOrder = TurnOrder.FIRST;
    TimeSetting timeSetting = new TimeSetting(false, 180, 1);

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
        game.turn = 1;

        //TODO
    }

    public void applySettings(Game game) {

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
