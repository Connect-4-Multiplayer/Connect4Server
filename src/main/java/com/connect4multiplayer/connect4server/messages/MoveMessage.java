package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Game;
import com.connect4multiplayer.connect4server.Move;
import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

public class MoveMessage extends Message {
    public MoveMessage() {
        this.type = MOVE;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        System.out.println(player.turn);
        System.out.println(player.game);
        player.enqueueMove(buffer.get());
        handleMove(player);
    }

    private void handleMove(Player player) {
        player.playMove().ifPresent(move -> {
            sendMoveToClients(move, player.game);
            handleMove(player.getOpponent());
        });
    }

    private void sendMoveToClients(Move move, Game game) {
        ByteBuffer buffer = constructMessage(7, move.col(), move.height(), move.player());
        byte gameState = game.getGameState();
        if (gameState != Game.WIN) {
            buffer.put(gameState).putShort((short) 0);
            game.host.client.write(buffer.flip());
            game.guest.client.write(buffer.flip());
        }
        else {
            byte winner = (byte) game.turn;
            buffer.put(winner).put(game.getWinningSpots());
            game.host.client.write(buffer.flip());
            // Flips winner bit since the other player has a victory when the first has a loss and vice versa
            buffer.put(4, (byte) (winner ^ 1));
            game.guest.client.write(buffer.flip());
        }
    }
}
