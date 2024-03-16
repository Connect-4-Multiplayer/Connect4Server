package com.connect4multiplayer.connect4server;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Optional;

public class Player {
    int turn = 1;
    AsynchronousSocketChannel client;
    Game game;

    public Player(AsynchronousSocketChannel client, Game game) {
        this.client = client;
        this.game = game
    }

    public Player(AsynchronousSocketChannel client) {
        this.client = client;
        this.game = null;
    }
}
