package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Game;
import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class Move extends Message {
    public Move() {

    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        player.enqueueMove(buffer.get());
        handleMove(player);
    }

    private void handleMove(Player player) {
        player.playMove().ifPresent(move -> {
            sendMoveToClients(move, player.game);
            handleMove(player.getOpponent());
        });
    }

    private void sendMoveToClients(com.connect4multiplayer.connect4server.Move move, Game game) {
        ByteBuffer buffer = constructReply(7, move.col(), move.height(), move.player());
        byte gameState = game.getGameState();
        if (gameState != Game.WIN) {
            buffer.put(gameState).putShort((short) 0);
            game.player1.client.write(buffer.flip());
            game.player2.client.write(buffer.flip());
        }
        else {
            byte winner = (byte) game.turn;
            buffer.put(winner).put(game.getWinningSpots());
            game.player1.client.write(buffer.flip());
            // Flips winner bit since the other player has a victory when the first has a loss and vice versa
            buffer.put(4, (byte) (winner ^ 1));
            game.player2.client.write(buffer.flip());
        }
    }
}
