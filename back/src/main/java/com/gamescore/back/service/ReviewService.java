package com.gamescore.back.service;

import com.gamescore.back.model.Review;
import com.gamescore.back.model.User;
import com.gamescore.back.model.Game;
import com.gamescore.back.model.enums.ReviewStatus;
import com.gamescore.back.repository.ReviewRepository;
import com.gamescore.back.repository.UserRepository;
import com.gamescore.back.repository.GameRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Usamos Lombok para inyección de dependencias a través del constructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    // --- LECTURA (READ) ---

    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> findReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    // Método que el DashboardController usa para cargar reseñas
    public List<Review> search(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return reviewRepository.searchWithGameAndUser(keyword);
        }
        return reviewRepository.findAllWithGameAndUser();
    }

    // Método existente (NO SE RECOMIENDA usar en el frontend ya que trae todos los
    // estados)
    public List<Review> findReviewsByGameId(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("Game not found with ID: " + gameId));
        return reviewRepository.findByGame(game);
    }

    // Obtiene solo las reseñas APROBADAS para mostrar en la vista de detalle.
    public List<Review> findApprovedReviewsByGameId(Long gameId) {
        return reviewRepository.findByGameIdAndStatus(gameId, ReviewStatus.APPROVED);
    }

    public long countPendingReviews() {
        return reviewRepository.countByStatus(ReviewStatus.PENDING);
    }

    // --- CREACIÓN (CREATE) ---

    @Transactional
    public Review createReview(Review review) {
        // 1. Validar y cargar referencias (User y Game)
        if (review.getUser() == null || review.getGame() == null) {
            throw new IllegalArgumentException("User ID and Game ID are required.");
        }

        // Se asume que el objeto Review que llega tiene los objetos User y Game con el
        // ID
        // correspondiente ya establecido desde el controlador.
        User user = userRepository.findById(review.getUser().getId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + review.getUser().getId()));
        Game game = gameRepository.findById(review.getGame().getId())
                .orElseThrow(() -> new NoSuchElementException("Game not found: " + review.getGame().getId()));

        // VALIDACIÓN: Impedir que un usuario cree una segunda reseña para el mismo
        // juego.
        if (reviewRepository.findByUserAndGame(user, game).isPresent()) {
            throw new IllegalArgumentException(
                    "You have already reviewed this game. You can edit your existing review instead.");
        }

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
     * Esta versión recibe la Review ya actualizada por el controlador.
     *
     * @param updatedReview La entidad Review completa (con ID) con los nuevos
     *                      datos.
     * @return La Review actualizada.
     */
    @Transactional
    public Review updateReview(Review updatedReview) {
        // 1. Verificar que la reseña a actualizar existe por ID
        Review existingReview = reviewRepository.findById(updatedReview.getId())
                .orElseThrow(() -> new NoSuchElementException("Review not found with ID: " + updatedReview.getId()));

        // 2. Aplicar las actualizaciones desde el objeto que viene del formulario.
        // El controlador ya se encargó de verificar la propiedad (autoría).

        // Campos editables por el usuario:
        existingReview.setTitle(updatedReview.getTitle());
        existingReview.setContent(updatedReview.getContent());
        existingReview.setRating(updatedReview.getRating());

        // 3. Re-establecer el estado a PENDING para que pase por moderación de nuevo
        existingReview.setStatus(ReviewStatus.PENDING);
        existingReview.setApprovedAt(null); // Limpiamos la fecha de aprobación anterior
        existingReview.setReviewedBy(null); // Limpiamos el moderador anterior
        existingReview.setReviewNote(null); // Limpiamos la nota de moderación anterior

        // Se mantienen las referencias originales a User y Game (no se cambian en la
        // edición)
        // El @UpdateTimestamp se encarga de actualizar el 'updatedAt'

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

    public List<Review> searchWithFilters(String keyword, ReviewStatus status, Integer rating) {

        return reviewRepository.findAll().stream()
                .filter(r -> keyword == null ||
                        keyword.isBlank() ||
                        r.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        r.getUser().getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        r.getGame().getName().toLowerCase().contains(keyword.toLowerCase()))
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> rating == null || r.getRating() >= rating)
                .toList();
    }

    public void updateReviewStatus(Long id, ReviewStatus status, String reviewNote, String adminEmail) {

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        User admin = userRepository.findByEmail(adminEmail)
                .orElse(null);

        review.setStatus(status);
        review.setReviewNote(reviewNote);
        review.setReviewedBy(admin);

        if (status == ReviewStatus.APPROVED) {
            review.setApprovedAt(LocalDateTime.now());
        } else {
            review.setApprovedAt(null); // Si se rechaza o queda pendiente
        }

        reviewRepository.save(review);
    }

    public void save(Review review) {
        reviewRepository.save(review);
    }

    public Page<Review> findAllPaged(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return reviewRepository.findAll(pageable);
    }

    public Page<Review> searchPagedWithFilters(String keyword, ReviewStatus status, Integer rating, int page,
            int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return reviewRepository.searchWithFilters(keyword, status, rating, pageable);
    }
}