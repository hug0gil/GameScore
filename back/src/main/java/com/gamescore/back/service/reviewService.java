package com.gamescore.back.service;

import com.gamescore.back.model.Review;
import com.gamescore.back.model.User; // Necesitas estas clases
import com.gamescore.back.model.Game; // Necesitas estas clases
import com.gamescore.back.model.enums.ReviewStatus;
import com.gamescore.back.repository.ReviewRepository;
import com.gamescore.back.repository.UserRepository;
import com.gamescore.back.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository; // Para buscar el User
    private final GameRepository gameRepository; // Para buscar el Game

    // Inyección de dependencias
    public ReviewService(ReviewRepository reviewRepository,
            UserRepository userRepository,
            GameRepository gameRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    // --- LECTURA (READ) ---

    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> findReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Review> findReviewsByGameId(Long gameId) {
        // Asumiendo que GameRepository tiene un findById que retorna Optional<Game>
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("Game not found with ID: " + gameId));
        return reviewRepository.findByGame(game);
    }

    // --- CREACIÓN (CREATE) ---

    @Transactional
    public Review createReview(Review review) {
        // 1. Validar y cargar referencias (User y Game)
        if (review.getUser() == null || review.getGame() == null) {
            throw new IllegalArgumentException("User ID and Game ID are required.");
        }

        User user = userRepository.findById(review.getUser().getId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + review.getUser().getId()));
        Game game = gameRepository.findById(review.getGame().getId())
                .orElseThrow(() -> new NoSuchElementException("Game not found: " + review.getGame().getId()));

        review.setUser(user);
        review.setGame(game);

        // 2. Establecer valores por defecto (si no están ya en el constructor/DTO)
        if (review.getStatus() == null) {
            review.setStatus(ReviewStatus.PENDING); // Nueva reseña entra como PENDIENTE
        }

        // El createdAt y updatedAt son manejados por las anotaciones
        // @CreationTimestamp/@UpdateTimestamp

        return reviewRepository.save(review);
    }

    // --- MODIFICACIÓN (UPDATE) ---

    /**
     * Permite al autor de la reseña modificar el título, contenido y rating.
     * 
     * @param id            El ID de la reseña a modificar.
     * @param updatedReview La entidad con los nuevos datos.
     * @return La Review actualizada.
     */
    @Transactional
    public Review updateReview(Long id, Review updatedReview) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Review not found with ID: " + id));

        // Actualizar campos permitidos por el usuario
        existingReview.setTitle(updatedReview.getTitle());
        existingReview.setContent(updatedReview.getContent());
        existingReview.setRating(updatedReview.getRating());

        // Opcional: Si se edita, se puede reestablecer el estado a PENDING para
        // moderación
        if (existingReview.getStatus() != ReviewStatus.PENDING) {
            existingReview.setStatus(ReviewStatus.PENDING);
            existingReview.setApprovedAt(null);
            existingReview.setReviewedBy(null);
            existingReview.setReviewNote(null);
        }

        return reviewRepository.save(existingReview);
    }

    /**
     * Operación de moderador para aprobar o rechazar una reseña.
     */
    @Transactional
    public Review moderateReview(Long reviewId, ReviewStatus newStatus, Long moderatorId, String reviewNote) {
        if (newStatus != ReviewStatus.APPROVED && newStatus != ReviewStatus.REJECTED) {
            throw new IllegalArgumentException("Invalid status for moderation.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review not found with ID: " + reviewId));

        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator User not found with ID: " + moderatorId));

        review.setStatus(newStatus);
        review.setReviewedBy(moderator);
        review.setReviewNote(reviewNote);

        if (newStatus == ReviewStatus.APPROVED) {
            review.setApprovedAt(LocalDateTime.now());
        } else {
            review.setApprovedAt(null);
        }

        return reviewRepository.save(review);
    }

    // --- ELIMINACIÓN (DELETE) ---

    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new NoSuchElementException("Review not found with ID: " + id);
        }
        reviewRepository.deleteById(id);
    }
}