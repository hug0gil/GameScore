package com.gamescore.back.repository;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.DTOs.GameListDTO;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    boolean existsByRawgId(Long rawgId);

    Optional<Game> findById(long id);

    @Query("select g.id as id, g.name as name, g.slug as slug, g.rating as rating, g.backgroundUrl as backgroundUrl from Game g")
    Page<GameListDTO> findAllLight(Pageable pageable);

    List<GameListDTO> findTop3ByOrderByRatingDesc();

    @Query("SELECT g FROM Game g JOIN FETCH g.genres JOIN FETCH g.platforms WHERE g.slug = :slug")
    Optional<Game> findBySlug(@Param("slug") String slug);
}