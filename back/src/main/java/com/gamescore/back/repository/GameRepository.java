package com.gamescore.back.repository;

import com.gamescore.back.model.Game;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    /**
     * Busca juegos donde el nombre contenga la palabra clave,
     * ignorando mayúsculas/minúsculas.
     * 
     * @param keyword La palabra clave a buscar.
     * @return Lista de juegos que coinciden.
     */

    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Game> searchByName(@Param("keyword") String keyword);

}