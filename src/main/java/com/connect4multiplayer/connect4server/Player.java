package com.connect4multiplayer.connect4server;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.LinkedList;
import java.util.Optional;

public class Player {
    public int turn = 1;
    public final AsynchronousSocketChannel client;
    public Lobby lobby;
    public Game game;

    public boolean isReady;
    public boolean isHost;

    final LinkedList<Move> moves = new LinkedList<>();
    public byte[] name;
    int[] moveHeights = new int[7];

    public Player(AsynchronousSocketChannel client) {
        this.client = client;
    }

    public synchronized void enqueueMove(byte col) {
        if (game == null) return;
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
        if (game == null) return Optional.empty();
        if (game.turn != turn || moves.isEmpty() || game.gameState != Game.NOT_OVER) return Optional.empty();
        Optional<Move> move = game.playMove(this);
        if (move.isEmpty()) clearMoveQueue();
        return move;
    }

    public synchronized void clearMoveQueue() {
        moveHeights = new int[7];
        moves.clear();
    }

    public Player getOpponent() {
        return turn == 0 ? game.host : game.guest;
    }
}
