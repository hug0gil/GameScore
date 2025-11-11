package com.gamescore.back.repository;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.Review;
import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // ========================================================================
    // BÚSQUEDAS BÁSICAS
    // ========================================================================
    
    /**
     * Busca reseña de un usuario para un juego
     * Usado en: Verificar si ya reseñó este juego (1 reseña por juego)
     */
    Optional<Review> findByUserAndGame(User user, Game game);
    
    /**
     * Verifica si el usuario ya reseñó un juego
     * Usado en: Validación antes de crear reseña
     */
    boolean existsByUserAndGame(User user, Game game);
    
    // ========================================================================
    // BÚSQUEDAS POR ESTADO
    // ========================================================================
    
    /**
     * Reseñas por estado con paginación
     * Usado en: Panel admin (pendientes, aprobadas, rechazadas)
     */
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);
    
    /**
     * Reseñas pendientes ordenadas por fecha (más antiguas primero)
     * Usado en: Cola de moderación
     */
    Page<Review> findByStatusOrderByCreatedAtAsc(ReviewStatus status, Pageable pageable);
    
    /**
     * Cuenta reseñas por estado
     * Usado en: Estadísticas del dashboard admin
     */
    long countByStatus(ReviewStatus status);
    
    // ========================================================================
    // BÚSQUEDAS POR USUARIO
    // ========================================================================
    
    /**
     * Todas las reseñas de un usuario
     * Usado en: Perfil de usuario (mis reseñas)
     */
    Page<Review> findByUser(User user, Pageable pageable);
    
    /**
     * Reseñas aprobadas de un usuario
     * Usado en: Perfil público (solo mostrar aprobadas)
     */
    Page<Review> findByUserAndStatus(User user, ReviewStatus status, Pageable pageable);
    
    /**
     * Cuenta reseñas de un usuario
     * Usado en: Estadísticas del perfil
     */
    long countByUser(User user);
    
    /**
     * Cuenta reseñas aprobadas de un usuario
     * Usado en: Perfil público
     */
    long countByUserAndStatus(User user, ReviewStatus status);
    
    // ========================================================================
    // BÚSQUEDAS POR JUEGO
    // ========================================================================
    
    /**
     * Reseñas aprobadas de un juego
     * Usado en: Página del juego (mostrar reseñas públicas)
     */
    Page<Review> findByGameAndStatus(Game game, ReviewStatus status, Pageable pageable);
    
    /**
     * Todas las reseñas de un juego (cualquier estado)
     * Usado en: Panel admin (ver todas las reseñas)
     */
    Page<Review> findByGame(Game game, Pageable pageable);
    
    /**
     * Cuenta reseñas aprobadas de un juego
     * Usado en: Mostrar "X reseñas" en tarjeta del juego
     */
    long countByGameAndStatus(Game game, ReviewStatus status);
    
    /**
     * Promedio de calificación de un juego (solo aprobadas)
     * Usado en: Calificación de usuarios en la página del juego
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.game = :game AND r.status = 'APPROVED'")
    Double getAverageRatingByGame(@Param("game") Game game);
    
    // ========================================================================
    // BÚSQUEDAS POR CALIFICACIÓN
    // ========================================================================
    
    /**
     * Reseñas con calificación específica o mayor
     * Usado en: Filtrar solo reseñas positivas (rating >= 7)
     */
    Page<Review> findByStatusAndRatingGreaterThanEqual(
        ReviewStatus status, 
        Integer minRating, 
        Pageable pageable
    );
    
    /**
     * Mejores reseñas (por rating)
     * Usado en: Destacar reseñas con mejor puntuación
     */
    Page<Review> findByStatusOrderByRatingDesc(ReviewStatus status, Pageable pageable);
    
    // ========================================================================
    // BÚSQUEDAS POR FECHA
    // ========================================================================
    
    /**
     * Reseñas en un rango de fechas
     * Usado en: Exportar reseñas del mes, reportes
     */
    @Query("SELECT r FROM Review r WHERE r.status = :status " +
           "AND r.createdAt BETWEEN :start AND :end")
    List<Review> findByStatusAndCreatedAtBetween(
        @Param("status") ReviewStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    /**
     * Reseñas aprobadas más recientes
     * Usado en: Página principal (últimas reseñas)
     */
    Page<Review> findByStatusOrderByApprovedAtDesc(ReviewStatus status, Pageable pageable);
    
    /**
     * Reseñas creadas recientemente
     * Usado en: Panel admin (nuevas reseñas)
     */
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // ========================================================================
    // BÚSQUEDAS POR MODERADOR
    // ========================================================================
    
    /**
     * Reseñas revisadas por un admin
     * Usado en: Actividad de moderadores
     */
    Page<Review> findByReviewedBy(User admin, Pageable pageable);
    
    /**
     * Cuenta reseñas moderadas por un admin
     * Usado en: Estadísticas de moderadores
     */
    long countByReviewedBy(User admin);
    
    // ========================================================================
    // BÚSQUEDA AVANZADA
    // ========================================================================
    
    /**
     * Búsqueda combinada por múltiples criterios
     * Usado en: Exportación con filtros
     */
    @Query("SELECT r FROM Review r WHERE " +
           "(:user IS NULL OR r.user = :user) AND " +
           "(:game IS NULL OR r.game = :game) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:minRating IS NULL OR r.rating >= :minRating)")
    Page<Review> findByAdvancedSearch(
        @Param("user") User user,
        @Param("game") Game game,
        @Param("status") ReviewStatus status,
        @Param("minRating") Integer minRating,
        Pageable pageable
    );
    
    // ========================================================================
    // ESTADÍSTICAS
    // ========================================================================
    
    /**
     * Top usuarios con más reseñas aprobadas
     * Usado en: Gráfico de usuarios más activos
     */
    @Query("SELECT r.user, COUNT(r) FROM Review r WHERE r.status = 'APPROVED' " +
           "GROUP BY r.user ORDER BY COUNT(r) DESC")
    List<Object[]> findTopReviewers(Pageable pageable);
    
    /**
     * Top juegos más reseñados
     * Usado en: Gráfico de juegos más populares
     */
    @Query("SELECT r.game, COUNT(r) FROM Review r WHERE r.status = 'APPROVED' " +
           "GROUP BY r.game ORDER BY COUNT(r) DESC")
    List<Object[]> findMostReviewedGames(Pageable pageable);
    
    /**
     * Distribución de calificaciones
     * Usado en: Gráfico de barras (cuántas reseñas 1-10)
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.status = 'APPROVED' " +
           "GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistribution();
}