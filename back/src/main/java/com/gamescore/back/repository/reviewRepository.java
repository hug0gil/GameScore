package com.gamescore.back.repository;

import com.gamescore.back.model.Review;
import com.gamescore.back.model.User;
import com.gamescore.back.model.Game;
import com.gamescore.back.model.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface reviewRepository extends JpaRepository<Review, Long> {

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
}