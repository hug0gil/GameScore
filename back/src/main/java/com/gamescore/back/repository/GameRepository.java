package com.gamescore.back.repository;

import com.gamescore.back.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    // JpaRepository ya nos da findAll(), save(), deleteById(), etc...
}