package com.gamescore.back.repository;

import com.gamescore.back.model.Review;
import com.gamescore.back.model.User;
import com.gamescore.back.model.Game;
import com.gamescore.back.model.enums.ReviewStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Busca todas las reseñas hechas por un usuario específico.
     * 
     * @param user El objeto User.
     * @return Lista de Reviews.
     */
    List<Review> findByUser(User user);

    /**
     * Busca todas las reseñas asociadas a un juego específico.
     * 
     * @param game El objeto Game.
     * @return Lista de Reviews.
     */
    List<Review> findByGame(Game game);

    /**
     * Busca todas las reseñas con un estado específico (ej: APPROVED, PENDING).
     * 
     * @param status El estado de la reseña (ReviewStatus).
     * @return Lista de Reviews.
     */
    List<Review> findByStatus(ReviewStatus status);

    // -------------------------------------------------------------------
    // --- NUEVOS MÉTODOS PARA LA FUNCIONALIDAD DE DETALLE DEL JUEGO ---
    // -------------------------------------------------------------------

    /**
     * Busca todas las reseñas asociadas a un juego con un estado específico.
     * 💡 Usado para obtener las reseñas APROBADAS en la vista pública.
     * 
     * @param game   El objeto Game.
     * @param status El estado de la reseña (ReviewStatus, ej: APPROVED).
     * @return Lista de Reviews.
     */
    List<Review> findByGameAndStatus(Game game, ReviewStatus status);

    /**
     * Busca si existe una reseña hecha por un usuario para un juego específico.
     * 💡 Se usa para validar que un usuario no cree más de una reseña por juego.
     * 
     * @param user El objeto User.
     * @param game El objeto Game.
     * @return Optional<Review> (vacío si no existe).
     */
    Optional<Review> findByUserAndGame(User user, Game game);

    @Query("""
                SELECT r
                FROM Review r
                JOIN FETCH r.user
                JOIN FETCH r.game
                WHERE (:keyword IS NULL
                       OR LOWER(r.game.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                       OR LOWER(r.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<Review> searchWithGameAndUser(@Param("keyword") String keyword);

    @Query("""
                SELECT r
                FROM Review r
                JOIN FETCH r.user
                JOIN FETCH r.game
            """)
    List<Review> findAllWithGameAndUser();

    long countByStatus(ReviewStatus status);

    List<Review> findByGameIdAndStatus(Long gameId, ReviewStatus status);

    @Query("""
                SELECT r FROM Review r
                WHERE (:keyword IS NULL OR LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:status IS NULL OR r.status = :status)
                  AND (:rating IS NULL OR r.rating = :rating)
            """)
    Page<Review> searchWithFilters(
            @Param("keyword") String keyword,
            @Param("status") ReviewStatus status,
            @Param("rating") Integer rating,
            Pageable pageable);
}
