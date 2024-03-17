package com.connect4multiplayer.connect4server;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.LinkedList;
import java.util.Optional;

public class Player {
    int turn = 1;
    AsynchronousSocketChannel client;
    Game game;
    final LinkedList<Move> moves = new LinkedList<>();
    int[] moveHeights = new int[7];

    public Player(AsynchronousSocketChannel client, Game game) {
        this.client = client;
        this.game = game;
    }

    public Player(AsynchronousSocketChannel client) {
        this.client = client;
        this.game = null;
    }

    public void enqueueMove(byte col) {
        game.validateMove(col, this).ifPresent(move -> {
            moveHeights[col]++;
            moves.add(move);
        });
        System.out.println(moves.size());
    }

    public Optional<Move> playMove() {
        return game.playMove(this);
    }

    public void clearPreMoves() {
        moveHeights = new int[7];
        moves.clear();
    }

    public Player getOpponent() {
        return turn == 0 ? game.player1 : game.player2;
    }
}