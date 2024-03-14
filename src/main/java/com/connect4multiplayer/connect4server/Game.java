package com.connect4multiplayer.connect4server;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Optional;

import static com.connect4multiplayer.connect4server.Engine.*;

/**
 * Keeps track of the current state of the game
 */
public class Game {
    /**
     * Number of spots on the board
     */
    static final int SPOTS = 42;
    /**
     * Represents the result of the game
     */
    static final byte WIN = 0, DRAW = 2, NOT_OVER = 3;
    /**
     * The current turn in the game
     */
    int turn = -1;
    /**
     * Number of moves made
     */
    int movesMade;
    /**
     * The current state of the game board
     */
    long state;
    AsynchronousSocketChannel player1Client, player2Client;

    public Game() {

    }

    /**
     * Plays a move
     * @param col The index of the column to play in
     */
    public Optional<Move> playMove(int col, int playerTurn) {
        if (!(playerTurn == turn && getHeight(col) != 6 && getGameState() == NOT_OVER)) return Optional.empty();
        int height = getHeight(col);
        state = nextState(state, turn, col, height);
        turn ^= 1;
        movesMade++;
        return Optional.of(new Move((byte) col, (byte) height, (byte) playerTurn));
    }

    /**
     * Gets the height of a column
     * @param col The index of the column
     * @return THe height of the column
     */
    public int getHeight(int col) {
        return (int) (state >>> SPOTS + col * 3 & 0b111);
    }

    /**
     * Checks if the game is over
     * @return Integer representing the result of the game
     */
    public byte getGameState() {
        if (isWin(state, turn ^ 1)) return WIN;
        if (movesMade == SPOTS) return DRAW;
        return NOT_OVER;
    }

    /**
     * Finds the four spots that create a win if one exists
     * @return Array containing the winning spots if they exist, null otherwise
     */
    public byte[] getWinningSpots() {
        long board = getPieceLocations(state, turn ^ 1);
        for (int i = 1; i < 9; i += (1 / i << 2) + 1) {
            long connections = board;
            for (byte j = 0; j < 3; j++) connections = connections & (connections >>> i);
            if (connections != 0) {
                byte start = (byte) Long.numberOfTrailingZeros(connections);
                start -= (byte) (start / 7);
                return new byte[]{start, (byte) (i - (i > 1 ? 1 : 0))};
            }
        }
        return null;
    }
}
