package com.connect4multiplayer.connect4server;

import com.connect4multiplayer.connect4server.messages.EngineMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Engine class responsible for calculating the AI's moves
 */
public class Engine {

    public int moveNum = 0;

    public Engine() {

    }

    private static enum Ranking {
        BEST,
        GOOD,
        INACCURACY,
        BLUNDER
    }

    static final int DATABASE_DEPTH = 15;
    /**
     * The worst minimax value possible
     */
    static final int WORST_EVAL = -18;
    /**
     * The best minimax value possible
     */
    static final int BEST_EVAL = 18;
    /**
     * The size of the cache of positions
     */
    static final int SIZE = 30_000_001;
    /**
     * Caches containing the positions
     */
    static long[] lowerBoundCache = new long[SIZE], upperBoundCache = new long[SIZE];
    /**
     * Caches containing the upper and lower bounds of their respective positions
     */
    static byte[] lowerBoundValues = new byte[SIZE], upperBoundValues = new byte[SIZE];
    static byte[] upperBoundDatabase = null;
    static byte[] lowerBoundDatabase = null;
    /**
     * The default order to search moves in
     */
    private static final int MOVE_ORDER = 3 + (2 << 4) + (4 << 8) + (5 << 12) + (1 << 16) + (6 << 20);
    static final int DEPTH = 15;
    static int[][][] combosCache = new int[DEPTH + 1][8][7];
    static final int[][] nCr = pascalsTriangle(DEPTH);
    static final int[][] comboSums = getComboSums(DEPTH);
    static final int[] posCount = totalPositionCounts(DEPTH);

//    static int[] evaluateMoves(long state, int movesMode) {
//        int piece = (movesMode & 1) ^ 1;
//        int[] moves = new int[7];
//        for ()
//    }

    /**
     * Finds the minimax value of a position, within the window [alpha, beta]
     * @param state The current position
     * @param piece The piece of the current player
     * @param alpha The lower bound of the search
     * @param beta The upper bound of the search
     * @param movesMade The number of moves made so far
     * @return Minimax value of the position within [alpha, beta]
     */
    static int evaluatePosition(long state, int piece, int alpha, int beta, int movesMade) {
        if (movesMade == 42) return 0;
        beta = Math.min(beta, 21 - (movesMade >>> 1));
        alpha = Math.max(alpha, (movesMade + 1 >>> 1) - 21);
        int index;
        if (movesMade > DATABASE_DEPTH) {
            index = (int) (state % SIZE);
            if (lowerBoundCache[index] == state) alpha = Math.max(alpha, lowerBoundValues[index]);
            if (upperBoundCache[index] == state) beta = Math.min(beta, upperBoundValues[index]);
        }
        else {
            index = getIndex(state, movesMade);
            alpha = Math.max(alpha, lowerBoundDatabase[index]);
            beta = Math.min(beta, upperBoundDatabase[index]);
        }
        if (alpha >= beta) return alpha;
        int threats = 0, order = MOVE_ORDER, forcedMoves = 0;
        long forcedMove = -1;
        for (int i = 0; i < 7; i++) {
            int col = MOVE_ORDER >> i * 4 & 0b1111;
            int height = (int) ((state >> 42 + col * 3) & 0b111);
            if (height != 6) {
                long move = nextState(state, piece, col, height), pieceLocations = getPieceLocations(move, piece);
                if (isWin(nextState(state, piece ^ 1, col, height), piece ^ 1)) {
                    forcedMoves++;
                    forcedMove = move;
                }
                if (isWin(pieceLocations)) return 21 - (movesMade >>> 1);
                if (movesMade < DATABASE_DEPTH) alpha = Math.max(alpha, -upperBoundDatabase[getIndex(move, movesMade + 1)]);
                else {
                    int moveIndex = (int) (move % SIZE);
                    if (upperBoundCache[moveIndex] == move) alpha = Math.max(alpha, -upperBoundValues[moveIndex]);
                }
                if (alpha >= beta) return alpha;
                threats += countThreats(move, pieceLocations, piece) << col * 4;
            }
        }
        if (forcedMoves > 0) return forcedMoves > 1 ? (movesMade + 1 >>> 1) - 21 : -evaluatePosition(forcedMove, piece ^ 1, -beta, -alpha, movesMade + 1);
        order = sortByThreats(order, threats);
        int i = 0;
        for (int j = 0; j < 28; j += 4) {
            int col = order >>> j & 0b1111;
            int height = (int) (state >>> 42 + col * 3 & 0b111);
            if (height != 6) {
                long move = nextState(state, piece, col, height);
                int eval;
                if (i++ == 0) eval = -evaluatePosition(move, piece ^ 1, -beta, -alpha, movesMade + 1);
                else {
                    eval = -evaluatePosition(move, piece ^ 1, -alpha - 1, -alpha, movesMade + 1);
                    if (eval > alpha && eval < beta) eval = -evaluatePosition(move, piece ^ 1, -beta, -alpha, movesMade + 1);
                }
                alpha = Math.max(alpha, eval);
                if (alpha >= beta) {
                    if (movesMade > DATABASE_DEPTH) {
                        lowerBoundCache[index] = state;
                        lowerBoundValues[index] = (byte) alpha;
                    }
                    else {
                        lowerBoundDatabase[index] = (byte) alpha;
                        int idx = getIndex(reflectState(state), movesMade);
                        lowerBoundDatabase[idx] = (byte) Math.max(alpha, lowerBoundDatabase[idx]);
                    }
                    return alpha;
                }
            }
        }
        if (movesMade > DATABASE_DEPTH) {
            upperBoundCache[index] = state;
            upperBoundValues[index] = (byte) alpha;
        }
        else {
            upperBoundDatabase[index] = (byte) alpha;
            int idx = getIndex(reflectState(state), movesMade);
            upperBoundDatabase[idx] = (byte) Math.min(alpha, upperBoundDatabase[idx]);
        }
        return alpha;
    }

    /**
     * Sorts moves based on the number of threats they create
     * @param order The order of moves
     * @param threats Stores the number of threats corresponding to each move
     * @return The order of moves
     */
    static int sortByThreats(int order, int threats) {
        for (int i = 4; i < 28; i += 4) {
            int j = i, currThreats = (threats >>> (order >>> i & 0b1111) * 4 & 0b1111);
            while (j > 0 && currThreats > (threats >>> (order >>> j - 4 & 0b1111) * 4 & 0b1111)) {
                j -= 4;
            }
            order = (order & (1 << j) - 1) + ((order >>> i & 0b1111) << j) + ((order >>> j & (1 << i - j) - 1) << j + 4) + (order >>> i + 4 << i + 4);
        }
        return order;
    }

    /**
     * Gets the next state that results after playing a move
     * @param state The current state
     * @param piece The piece of the player making the move
     * @param col The col that is being played in
     * @param height The height of the column being played in
     * @return The resulting state after the move is made
     */
    static long nextState(long state, int piece, int col, int height){
        if(piece == 1) state += 1L << col * 6 + height;
        return state + (1L << 42 + col * 3);
    }

    /**
     * Counts the number of winning threats for a player in the current position
     * @param state The current state
     * @param pieceLocations The locations of all pieces the current player has played
     * @param piece The piece of the current player
     * @return Number of winning threats
     */
    static int countThreats(long state, long pieceLocations, int piece) {
        int threatCount = 0;
        for (int col = 0; col < 7; col++) {
            for (int row = (int) (state >>> 42 + col * 3 & 0b111); row < 6; row++) {
                if (isWin(pieceLocations + (1L << col * 7 + row))) threatCount += 1 + ((row & 1) ^ piece);
            }
        }
        return threatCount;
    }

    /**
     * Finds all spots where the current player has played
     * @param state The current state
     * @param piece The piece of the current player
     * @return All spots where the current player has played
     */
    static long getPieceLocations(long state, int piece) {
        long board = 0;
        if (piece == 1) {
            for (int i = 0; i < 7; i++) {
                board += (state >>> (6 * i) & 0b111111) << (7 * i);
            }
        }
        else {
            for (int i = 0; i < 7; i++) {
                board += ((state >>> (6 * i) & 0b111111) ^ ((1 << (state >> (42 + i * 3) & 0b111)) - 1)) << (7 * i);
            }
        }
        return board;
    }

    /**
     * Determines if the current player has won the game
     * @param pieceLocations The spots where the current player has played
     * @return true if the current player has won, false otherwise
     */
    static boolean isWin(long pieceLocations) {
        for (int i = 1; i < 9; i += 1 / i * 4 + 1) {
            long connections = pieceLocations;
            for (int j = 0; j < 3; j++) connections = connections & (connections >>> i);
            if (connections != 0) return true;
        }
        return false;
    }

    /**
     * Determines if the current player has won the game
     * @param state The current state
     * @param piece The piece of the current player
     * @return true if the current player has won, false otherwise
     */
    static boolean isWin(long state, int piece) {
        return isWin(getPieceLocations(state, piece));
    }

    /**
     * Gets the state that is equivalent to the current state, except reflected horizontally
     * @param state The current state
     * @return The horizontally reflected state
     */
    static long reflectState(long state) {
        long reflected = 0;
        for (int col = 0; col < 7; col++) {
            reflected += ((state >>> col * 6 & 0b111111) << (6 - col) * 6) + ((state >>> 42 + col * 3 & 0b111) << 42 + (6 - col) * 3);
        }
        return reflected;
    }

    static int getIndex(long state, int pieces) {
        if (pieces == 0) return 0;
        int index = posCount[pieces - 1];
        int combos = nCr[pieces][pieces >>> 1];
        int p2Pieces = pieces >>> 1, p1Pieces = pieces - p2Pieces;
        for (int col = 0; col < 7 && pieces > 0; col++) {
            int height = (int) (state >>> 42 + col * 3 & 0b111);
            index += (comboSums[6 - col][pieces] - comboSums[6 - col][pieces - height]) * combos;
            for (int row = 0; row < height && p1Pieces > 0 && p2Pieces > 0; row++) {
                if ((state >>> col * 6 + row & 1) == 1) {
                    index += nCr[pieces - row - 1][p1Pieces];
                    p1Pieces--;
                }
                else p2Pieces--;
            }
            pieces -= height;
        }
        return index;
    }

    private static int[] totalPositionCounts(int n) {
        int[] counts = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            counts[i] = (int) positionsCount(i);
        }
        return counts;
    }

    static long positionsCount(int pieces) {
        long sum = 0;
        for (int i = 0; i <= pieces; i++) {
            sum += combos(i) * nCr[i][i >>> 1];
        }
        return sum;
    }

    static int[][] pascalsTriangle(int n) {
        int[][] pascalsTriangle = new int[n + 1][];
        pascalsTriangle[0] = new int[]{1};
        for (int i = 1; i <= n; i++) {
            int[] prevRow = pascalsTriangle[i - 1];
            int[] row = pascalsTriangle[i] = new int[i + 1];
            row[0] = 1;
            row[i] = 1;
            for (int j = 1; j < i; j++) {
                row[j] = prevRow[j - 1] + prevRow[j];
            }
        }
        return pascalsTriangle;
    }

    static int[][] getComboSums(int n) {
        int[][] comboSums = new int[8][n + 1];
        for (int cols = 0; cols <= 7; cols++) {
            int[] sums = comboSums[cols] = new int[n + 1];
            sums[0] = 1;
            for (int pieces = 1; pieces <= n; pieces++) {
                sums[pieces] = (int) combos(pieces, cols) + sums[pieces - 1];
            }
        }
        return comboSums;
    }

    static long combos(int pieces) {
        return combos(pieces, 7, 0);
    }

    static long combos(int pieces, int cols) {
        return combos(pieces, cols, 0);
    }

    static long combos(int pieces, int cols, int height) {
        if (pieces == 0) return 1;
        if (cols == 0 && pieces > 0) return 0;
        if (combosCache[pieces][cols][height] != 0) return combosCache[pieces][cols][height];
        long total = combos(pieces, cols - 1, 0);
        if (height < 6) total += combos(pieces - 1, cols, height + 1);
        combosCache[pieces][cols][height] = (int) total;
        return total;
    }

    static byte[] loadDatabase(String file) {
        try (FileInputStream in = new FileInputStream(file)) {
            return in.readAllBytes();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
