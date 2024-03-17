package com.connect4multiplayer.connect4server;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.LinkedList;

public class Player {
    int turn = 1;
    AsynchronousSocketChannel client;
    Game game;
    final LinkedList<Move> moves = new LinkedList<>();

    public Player(AsynchronousSocketChannel client, Game game) {
        this.client = client;
        this.game = game;
    }

    public Player(AsynchronousSocketChannel client) {
        this.client = client;
        this.game = null;
    }
}
