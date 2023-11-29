package com.example.tictactoe.service;

import com.example.tictactoe.model.TicTacToe;
import com.example.tictactoe.repository.TicTacToeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicTacToeService {
    private final TicTacToeRepository gameRepository;

    @Autowired
    public TicTacToeService(TicTacToeRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public TicTacToe saveGame(TicTacToe game) {
        return gameRepository.save(game);
    }

    public TicTacToe findGameById(Long gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }
}
