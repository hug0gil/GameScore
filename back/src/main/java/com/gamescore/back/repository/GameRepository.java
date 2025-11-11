package com.gamescore.back.repository;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.Genre;
import com.gamescore.back.model.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    // ========================================================================
    // BÚSQUEDAS BÁSICAS
    // ========================================================================
    
    /**
     * Busca juego por slug
     * Usado en: URLs amigables (/games/elden-ring)
     */
    Optional<Game> findBySlug(String slug);
    
    /**
     * Busca juego por ID de RAWG
     * Usado en: Evitar duplicados al importar desde RAWG
     */
    Optional<Game> findByRawgId(Long rawgId);
    
    /**
     * Verifica si existe un juego por nombre
     * Usado en: Validación antes de crear
     */
    boolean existsByName(String name);
    
    /**
     * Verifica si existe por slug
     * Usado en: Validación de URLs únicas
     */
    boolean existsBySlug(String slug);
    
    // ========================================================================
    // BÚSQUEDAS POR NOMBRE
    // ========================================================================
    
    /**
     * Busca juegos por nombre (case insensitive, parcial)
     * Usado en: Buscador principal
     */
    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Game> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    /**
     * Busca en nombre y descripción
     * Usado en: Búsqueda avanzada
     */
    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(g.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Game> searchGames(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // ========================================================================
    // BÚSQUEDAS POR GÉNERO
    // ========================================================================
    
    /**
     * Juegos de un género específico
     * Usado en: Filtrar por género (/games?genre=rpg)
     */
    @Query("SELECT DISTINCT g FROM Game g JOIN g.genres gen WHERE gen = :genre")
    Page<Game> findByGenre(@Param("genre") Genre genre, Pageable pageable);
    
    /**
     * Juegos de varios géneros (OR)
     * Usado en: Filtro múltiple de géneros
     */
    @Query("SELECT DISTINCT g FROM Game g JOIN g.genres gen WHERE gen IN :genres")
    Page<Game> findByGenresIn(@Param("genres") List<Genre> genres, Pageable pageable);
    
    // ========================================================================
    // BÚSQUEDAS POR PLATAFORMA
    // ========================================================================
    
    /**
     * Juegos de una plataforma específica
     * Usado en: Filtrar por plataforma (/games?platform=pc)
     */
    @Query("SELECT DISTINCT g FROM Game g JOIN g.platforms p WHERE p = :platform")
    Page<Game> findByPlatform(@Param("platform") Platform platform, Pageable pageable);
    
    /**
     * Juegos de varias plataformas (OR)
     * Usado en: Filtro múltiple de plataformas
     */
    @Query("SELECT DISTINCT g FROM Game g JOIN g.platforms p WHERE p IN :platforms")
    Page<Game> findByPlatformsIn(@Param("platforms") List<Platform> platforms, Pageable pageable);
    
    // ========================================================================
    // BÚSQUEDAS POR CALIFICACIÓN
    // ========================================================================
    
    /**
     * Juegos con calificación mayor o igual
     * Usado en: Filtrar solo juegos bien calificados
     */
    Page<Game> findByRatingGreaterThanEqual(BigDecimal minRating, Pageable pageable);
    
    /**
     * Juegos entre rango de calificación
     * Usado en: Filtro de calidad (4.0 - 5.0)
     */
    Page<Game> findByRatingBetween(BigDecimal minRating, BigDecimal maxRating, Pageable pageable);
    
    /**
     * Mejores juegos por rating
     * Usado en: Top juegos recomendados
     */
    Page<Game> findAllByOrderByRatingDesc(Pageable pageable);
    
    // ========================================================================
    // BÚSQUEDAS POR FECHA
    // ========================================================================
    
    /**
     * Juegos lanzados en un rango de fechas
     * Usado en: Filtrar por año (2023, 2024)
     */
    Page<Game> findByReleaseDateBetween(LocalDate start, LocalDate end, Pageable pageable);
    
    /**
     * Juegos más recientes
     * Usado en: Sección "Nuevos lanzamientos"
     */
    Page<Game> findAllByOrderByReleaseDateDesc(Pageable pageable);
    
    /**
     * Próximos lanzamientos
     * Usado en: Sección "Próximamente"
     */
    @Query("SELECT g FROM Game g WHERE g.releaseDate > CURRENT_DATE ORDER BY g.releaseDate ASC")
    Page<Game> findUpcomingGames(Pageable pageable);
    
    // ========================================================================
    // BÚSQUEDAS COMBINADAS
    // ========================================================================
    
    /**
     * Búsqueda avanzada combinando múltiples filtros
     * Usado en: Buscador avanzado con múltiples criterios
     */
    @Query("SELECT DISTINCT g FROM Game g " +
           "LEFT JOIN g.genres gen " +
           "LEFT JOIN g.platforms plat " +
           "WHERE (:name IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:genre IS NULL OR gen = :genre) " +
           "AND (:platform IS NULL OR plat = :platform) " +
           "AND (:minRating IS NULL OR g.rating >= :minRating)")
    Page<Game> findByAdvancedSearch(
        @Param("name") String name,
        @Param("genre") Genre genre,
        @Param("platform") Platform platform,
        @Param("minRating") BigDecimal minRating,
        Pageable pageable
    );
    
    // ========================================================================
    // ESTADÍSTICAS
    // ========================================================================
    
    /**
     * Cuenta juegos por género
     * Usado en: Gráfico de géneros más populares
     */
    @Query("SELECT gen.name, COUNT(g) FROM Game g JOIN g.genres gen GROUP BY gen.name ORDER BY COUNT(g) DESC")
    List<Object[]> countGamesByGenre();
    
    /**
     * Cuenta juegos por plataforma
     * Usado en: Gráfico de plataformas más populares
     */
    @Query("SELECT p.name, COUNT(g) FROM Game g JOIN g.platforms p GROUP BY p.name ORDER BY COUNT(g) DESC")
    List<Object[]> countGamesByPlatform();
    
    /**
     * Juegos agregados recientemente
     * Usado en: Panel admin (últimos juegos agregados)
     */
    Page<Game> findAllByOrderByCreatedAtDesc(Pageable pageable);
}