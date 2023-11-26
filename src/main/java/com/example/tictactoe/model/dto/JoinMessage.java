package com.example.tictactoe.model.dto;

import lombok.Data;

@Data
public class JoinMessage implements Message {
    private String type;
    private String gameId;
    private String player;
    private String content;
}
