package com.example.tictactoe.repository;

import com.example.tictactoe.model.TicTacToe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicTacToeRepository extends JpaRepository<TicTacToe, Long> {
    List<TicTacToe> findTop10ByOrderByLastMoveTimeAsc();
}
