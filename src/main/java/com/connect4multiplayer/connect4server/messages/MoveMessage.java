package com.connect4multiplayer.connect4server.messages;

import com.connect4multiplayer.connect4server.Game;
import com.connect4multiplayer.connect4server.Move;
import com.connect4multiplayer.connect4server.Player;
import com.connect4multiplayer.connect4server.Server;

import java.nio.ByteBuffer;

import static com.connect4multiplayer.connect4server.Game.NOT_OVER;
import static com.connect4multiplayer.connect4server.messages.SetSetting.TURN_ORDER;

public class MoveMessage extends Message {

    private static final byte MOVE = 0, PRE_MOVE = 1, DELETE_PRE_MOVE = 2;

    public MoveMessage() {
        this.type = MOVE_MESSAGE;
    }

    @Override
    public void process(Server server, Player player, ByteBuffer buffer) {
        switch (buffer.get()) {
            case MOVE -> {
                player.enqueueMove(buffer.get());
                handleMove(player, player.getOpponent());
            }
            case DELETE_PRE_MOVE -> {
                player.deletePreMove();
                sendPreMoves(player);
            }
        }
    }

    private void handleMove(Player player, Player opponent) {
        player.playMove().ifPresentOrElse(move -> {
            sendMoveToClients(move, player.game);
            if (player.game.gameState != NOT_OVER) {
                player.lobby.updateSettingsAfterGame();
                new SetSetting().sendSetting(player.lobby, TURN_ORDER, (byte) player.lobby.turnOrder, (byte) 0);
            }
            else handleMove(opponent, player);
        }, () -> {
            sendPreMoves(player);
            sendPreMoves(opponent);
        });
    }

    private void sendMoveToClients(Move move, Game game) {
        int MOVE_SIZE = 8;
        ByteBuffer buffer = constructMessage(MOVE_SIZE, MOVE, move.col(), move.height(), move.player());
        byte gameState = game.gameState;
        if (gameState != Game.WIN) {
            buffer.put(gameState).putShort((short) 0);
            game.host.client.write(buffer.flip());
            game.guest.client.write(buffer.flip());
        }
        else {
            byte winner = (byte) (game.turn ^ game.guest.turn);
            buffer.put(winner).put(game.getWinningSpots());
            game.host.client.write(buffer.flip());
            // Flips winner bit since the other player has a victory when the first has a loss and vice versa
            buffer.put(5, (byte) (winner ^ 1));
            game.guest.client.write(buffer.flip());
        }
    }

    public void sendPreMoves(Player player) {
        int PRE_MOVES_SIZE = 9;
        ByteBuffer buffer = constructMessage(PRE_MOVES_SIZE, PRE_MOVE);
        byte[] colCounts = new byte[7];
        for (Move move : player.moves) {
            colCounts[move.col()]++;
        }
        player.client.write(buffer.put(colCounts).flip());
    }
}
