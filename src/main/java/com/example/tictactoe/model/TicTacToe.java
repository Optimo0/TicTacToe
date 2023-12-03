package com.example.tictactoe.model;

import com.example.tictactoe.converter.BoardConverter;
import com.example.tictactoe.enumeration.GameState;
import jakarta.persistence.*;
import lombok.Data;


import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Class representing a Tic-Tac-Toe game.
 */
@Entity
@Table(name = "tic_tac_toe")
@Data
public class TicTacToe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String gameId;
    @Lob
    @Convert(converter = BoardConverter.class)
    @Column(length = 10000)
    private String[][] board;
    private String player1;
    private String player2;
    private String winner;
    private String turn;
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastMoveTime;
    private GameState gameState;
    public static final int BOARD_SIZE = 20;
    private static final int WINNING_LENGTH = 5;
    private static final long MOVE_TIME_LIMIT_MS = 30 * 1000; // 30 seconds per move
    private static final long GAME_TIME_LIMIT_MS = 15 * 60 * 1000; // 15 minutes per game
    private long currentPlayerMoveStartTime;
    private long totalGameStartTime;
    private boolean timeout = false;
    private String timedOutPlayer;

    public TicTacToe() {}

    public TicTacToe(String player1, String player2) {
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
        startGame();
    }

    public void startGame() {
        this.startTime = new Date();
        totalGameStartTime = System.currentTimeMillis();
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
            lastMoveTime = new Date();
            checkWinner();
            updateGameState();
            startMoveTimer();
            if (isMoveTimeLimitExceeded()) {
                timeout = true;
                timedOutPlayer = player;  // Set the timed-out player
                updateGameState();
            }
        }
    }

    public boolean hasTimedOut(String player) {
        return timedOutPlayer != null && timedOutPlayer.equals(player);
    }

    /**
     * Check if there is a winner. If a winning combination is found,
     * the winner is set to the corresponding player.
     */
    private void checkWinner() {
        boolean isTie = true;

        // Check horizontally, vertically, and diagonally
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (checkSequence(i, j, 1, 0) ||   // Check horizontally
                        checkSequence(i, j, 0, 1) ||   // Check vertically
                        checkSequence(i, j, 1, 1) ||   // Check diagonally (top-left to bottom-right)
                        checkSequence(i, j, 1, -1)) {  // Check diagonally (top-right to bottom-left)
                    isTie = false;
                    break;
                }
            }
            if (!isTie) {
                break;
            }
        }

        // If no winner and the board is full, set winner to "TIE"
        if (isTie && isBoardFull() && winner == null) {
            setWinner("TIE");
        }
    }

    /**
     * Helper method to check for winning sequence starting at position (i, j)
     */
    private boolean checkSequence(int row, int col, int rowIncrement, int colIncrement) {
        int endRow = row + (WINNING_LENGTH - 1) * rowIncrement;
        int endCol = col + (WINNING_LENGTH - 1) * colIncrement;

        // Check if the sequence is within bounds
        if (endRow >= 0 && endRow < BOARD_SIZE && endCol >= 0 && endCol < BOARD_SIZE) {
            String mark = getMark(row, col);

            // Check for a winning sequence
            for (int k = 0; k < WINNING_LENGTH; k++) {
                if (!Objects.equals(board[row + k * rowIncrement][col + k * colIncrement], mark) ||
                        Objects.equals(board[row][col], " ")) {
                    return false;
                }
            }

            // If a winning sequence is found, set the winner
            setWinner(mark);
            return true;
        }

        return false;
    }

    private String getMark(int row, int col) {
        return board[row][col];
    }

    public void startMoveTimer() {
        currentPlayerMoveStartTime = System.currentTimeMillis();
    }

    public boolean isMoveTimeLimitExceeded() {
        long elapsedTime = System.currentTimeMillis() - currentPlayerMoveStartTime;
        return elapsedTime > MOVE_TIME_LIMIT_MS;
    }

    public boolean isGameTimeLimitExceeded() {
        long elapsedTime = System.currentTimeMillis() - totalGameStartTime;
        return elapsedTime > GAME_TIME_LIMIT_MS;
    }

    /**
     * Updates the game state based on the current state of the game.
     */
    public void updateGameState() {
        if (isGameTimeLimitExceeded()) {
            gameState = GameState.TIME_LIMIT_EXCEEDED;
            timeout = true;
            return;
        }

        if (timeout) {
            turn = player1.equals(turn) ? player2 : player1;  // Change turn when timeout occurs
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

