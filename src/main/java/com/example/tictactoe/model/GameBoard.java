package com.example.tictactoe.model;

import jakarta.persistence.*;
import lombok.Data;


import java.util.Arrays;

@Entity
@Table(name = "game_board")
@Data
public class GameBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String boardState;

    // Add a default (no-argument) constructor
    public GameBoard() {
    }

    public String[][] getBoard() {
        // Convert the stored JSON string to a 2D array
        return Arrays.stream(boardState.split(","))
                .map(row -> row.split(""))
                .toArray(String[][]::new);
    }

    public void setBoard(String[][] board) {
        // Convert the 2D array to a JSON string for storage
        this.boardState = Arrays.stream(board)
                .map(row -> String.join("", row))
                .reduce((row1, row2) -> row1 + "," + row2)
                .orElse("");
    }
}