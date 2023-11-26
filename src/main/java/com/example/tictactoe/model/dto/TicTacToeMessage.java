package com.example.tictactoe.model.dto;

import com.example.tictactoe.model.TicTacToe;
import com.example.tictactoe.enumeration.GameState;
import lombok.Data;

@Data
public class TicTacToeMessage implements Message {
    private String type;
    private String gameId;
    private String player1;
    private String player2;
    private String winner;
    private String turn;
    private String content;
    private String[][] board;
    private int move;
    private GameState gameState;
    private String sender;

    public TicTacToeMessage() {
    }

    public TicTacToeMessage(TicTacToe game) {
        this.gameId = game.getGameId();
        this.player1 = game.getPlayer1();
        this.player2 = game.getPlayer2();
        this.winner = game.getWinner();
        this.turn = game.getTurn();
        this.board = game.getBoard();
        this.gameState = game.getGameState();
    }
}
