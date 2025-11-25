package com.gamescore.back.repository;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.DTOs.GameListDTO;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = { "genres", "platforms" })
    @Query("SELECT g FROM Game g WHERE lower(trim(g.slug)) = lower(trim(:slug))")
    Optional<Game> findBySlug(@Param("slug") String slug);

    Page<Game> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT g FROM Game g " +
            "LEFT JOIN FETCH g.genres " +
            "LEFT JOIN FETCH g.platforms " +
            "WHERE g.slug = :slug")
    Optional<Game> findBySlugWithGenresAndPlatforms(@Param("slug") String slug);
}