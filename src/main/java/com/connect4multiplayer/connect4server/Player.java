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

    public final LinkedList<Move> moves = new LinkedList<>();
    public byte[] name = new byte[64];
    public int[] moveHeights = new int[7];

    public Player(AsynchronousSocketChannel client) {
        this.client = client;
    }

    public synchronized void enqueueMove(byte col) {
        if (game == null) return;
        game.validateMove(col, this).ifPresent(move -> {
            moveHeights[col]++;
            moves.add(move);
        });
    }

    public synchronized void deletePreMove() {
        if (!moves.isEmpty()) {
            Move move = moves.pollLast();
            moveHeights[move.col()]--;
        }
    }

    public synchronized Optional<Move> playMove() {
        if (game == null || game.turn != turn || moves.isEmpty() || game.gameState != Game.NOT_OVER) return Optional.empty();
        Optional<Move> move = game.playMove(this);
        if (move.isEmpty()) clearMoveQueue();
        return move;
    }

    public synchronized void clearMoveQueue() {
        moveHeights = new int[7];
        moves.clear();
    }

    public Player getOpponent() {
        return lobby.host == this ? lobby.guest : lobby.host;
    }
}
