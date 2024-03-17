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

    public synchronized void enqueueMove(byte col) {
        game.validateMove(col, this).ifPresent(move -> {
            moveHeights[col]++;
            moves.add(move);

        });
        for (Move move : moves) {
            System.out.print("Player " + move.player());
            System.out.print(" Col " + move.col());
            System.out.print(" Height " + move.height());
            System.out.print(", ");
        }
        System.out.println();
    }

    public synchronized Optional<Move> playMove() {
        if (game.turn != turn || moves.isEmpty()) return Optional.empty();
        Optional<Move> move = game.playMove(this);
        if (move.isEmpty()) clearPreMoves();
        System.out.println("Rows " + game.getHeight(3));
        return move;
    }

    public synchronized void clearPreMoves() {
        moveHeights = new int[7];
        moves.clear();
    }

    public Player getOpponent() {
        return turn == 0 ? game.player1 : game.player2;
    }
}
