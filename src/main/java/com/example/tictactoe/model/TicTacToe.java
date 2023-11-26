package com.example.tictactoe.model;

import com.example.tictactoe.enumeration.GameState;
import lombok.Data;

import java.util.Objects;
import java.util.UUID;

/**
 * Class representing a Tic-Tac-Toe game.
 *
 * @author Joabson Arley do Nascimento
 */

@Data
public class TicTacToe {
    private String gameId;
    private String[][] board;
    private String player1;
    private String player2;
    private String winner;
    private String turn;
    private GameState gameState;
    public static final int BOARD_SIZE = 5;
    private static final int WINNING_LENGTH = 5;
    private static final long TOTAL_GAME_TIME = 15 * 60 * 1000; // 15 minutes in milliseconds
    private static final long MOVE_TIME_LIMIT = 30 * 1000; // 30 seconds in milliseconds
    private long startTime;
    private long lastMoveTime;
    private boolean timeout = false;

    public TicTacToe(String player1, String player2) {
        this.startTime = System.currentTimeMillis();
        this.gameId = UUID.randomUUID().toString();
        this.player1 = player1;
        this.player2 = player2;
        this.turn = player1;
        this.board = new String[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = " ";
            }
        }
        gameState = GameState.WAITING_FOR_PLAYER;
    }

    /**
     * Makes a move in the specified position on the board.
     *
     * @param player the name of the player making the move
     * @param move   the position of the move
     */
    public void makeMove(String player, int move) {
        int row = move / BOARD_SIZE;
        int col = move % BOARD_SIZE;
        if (Objects.equals(board[row][col], " ")) {
            board[row][col] = Objects.equals(player, player1) ? "X" : "O";
            turn = player.equals(player1) ? player2 : player1;
            checkWinner();
            updateGameState();
        }
    }

    /**
     * Check if there is a winner. If a winning combination is found,
     * the winner is set to the corresponding player.
     */

//    private void checkWinner() {
//        for (int i = 0; i < BOARD_SIZE; i++) {
//            for (int j = 0; j <= BOARD_SIZE - WINNING_LENGTH; j++) {
//                if (checkConsecutiveMarks(i, j, 1, 0)) {
//                    setWinner(getMark(i, j));
//                    return;
//                }
//            }
//        }
//
//        for (int i = 0; i <= BOARD_SIZE - WINNING_LENGTH; i++) {
//            for (int j = 0; j < BOARD_SIZE; j++) {
//                if (checkConsecutiveMarks(i, j, 0, 1)) {
//                    setWinner(getMark(i, j));
//                    return;
//                }
//            }
//        }
//
//        for (int i = 0; i <= BOARD_SIZE - WINNING_LENGTH; i++) {
//            for (int j = 0; j <= BOARD_SIZE - WINNING_LENGTH; j++) {
//                if (checkConsecutiveMarks(i, j, 1, 1) || checkConsecutiveMarks(i, j + WINNING_LENGTH - 1, 1, -1)) {
//                    setWinner(getMark(i, j));
//                    return;
//                }
//            }
//        }
//    }


    private void checkWinner() {
        // Check horizontally
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j <= BOARD_SIZE - 5; j++) {
                boolean isWinningSequence = true;
                for (int k = 0; k < 5; k++) {
                    if (!Objects.equals(board[i][j + k], board[i][j]) || Objects.equals(board[i][j], " ")) {
                        isWinningSequence = false;
                        break;
                    }
                }
                if (isWinningSequence) {
                    setWinner(getMark(i, j));
                    return;
                }
            }
        }

        // Check vertically
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j <= BOARD_SIZE - 5; j++) {
                boolean isWinningSequence = true;
                for (int k = 0; k < 5; k++) {
                    if (!Objects.equals(board[j + k][i], board[j][i]) || Objects.equals(board[j][i], " ")) {
                        isWinningSequence = false;
                        break;
                    }
                }
                if (isWinningSequence) {
                    setWinner(getMark(i, j));
                    return;
                }
            }
        }

        // Check diagonally (top-left to bottom-right)
        for (int i = 0; i <= BOARD_SIZE - 5; i++) {
            for (int j = 0; j <= BOARD_SIZE - 5; j++) {
                boolean isWinningSequence = true;
                for (int k = 0; k < 5; k++) {
                    if (!Objects.equals(board[i + k][j + k], board[i][j]) || Objects.equals(board[i][j], " ")) {
                        isWinningSequence = false;
                        break;
                    }
                }
                if (isWinningSequence) {
                    setWinner(getMark(i, j));
                    return;
                }
            }
        }

        // Check diagonally (top-right to bottom-left)
        for (int i = 0; i <= BOARD_SIZE - 5; i++) {
            for (int j = BOARD_SIZE - 1; j >= 4; j--) {
                boolean isWinningSequence = true;
                for (int k = 0; k < 5; k++) {
                    if (!Objects.equals(board[i + k][j - k], board[i][j]) || Objects.equals(board[i][j], " ")) {
                        isWinningSequence = false;
                        break;
                    }
                }
                if (isWinningSequence) {
                    setWinner(getMark(i, j));
                    return;
                }
            }
        }
    }

    private boolean checkConsecutiveMarks(int row, int col, int rowIncrement, int colIncrement) {
        String mark = getMark(row, col);
        for (int i = 1; i < WINNING_LENGTH; i++) {
            if (!Objects.equals(mark, getMark(row + i * rowIncrement, col + i * colIncrement))) {
                return false;
            }
        }
        return true;
    }

    private String getMark(int row, int col) {
        return board[row][col];
    }

    /**
     * Updates the game state based on the current state of the game.
     */
    private void updateGameState() {
        if (timeout) {
            gameState = turn.equals(player1) ? GameState.PLAYER2_WON : GameState.PLAYER1_WON;
        } else if (winner != null) {
            gameState = turn.equals(player1) ? GameState.PLAYER1_WON : GameState.PLAYER2_WON;
        } else if (isBoardFull()) {
            gameState = GameState.TIE;
        } else {
            gameState = turn.equals(player1) ? GameState.PLAYER1_TURN : GameState.PLAYER2_TURN;
        }
    }

    /**
     * Check if the board is full.
     *
     * @return true if the board is full, false otherwise
     */
    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (Objects.equals(board[i][j], " ")) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if the game is over.
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return winner != null || isBoardFull();
    }
}

