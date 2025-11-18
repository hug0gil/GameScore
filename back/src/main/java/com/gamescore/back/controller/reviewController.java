package com.gamescore.back.controller;

import com.gamescore.back.model.Review;
import com.gamescore.back.model.User; // Necesaria para la moderación
import com.gamescore.back.model.enums.ReviewStatus;
import com.gamescore.back.service.reviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/reviews")
public class reviewController {

    private final reviewService reviewService;

    public reviewController(reviewService reviewService) {
        this.reviewService = reviewService;
    }

    // --- GET: Listar todas las reseñas ---
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.findAllReviews();
        return ResponseEntity.ok(reviews);
    }

    // --- GET: Obtener reseña por ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return reviewService.findReviewById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- GET: Obtener reseñas por Game ID ---
    // Endpoint recomendado para la vista de un juego específico
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<Review>> getReviewsByGame(@PathVariable Long gameId) {
        try {
            List<Review> reviews = reviewService.findReviewsByGameId(gameId);
            return ResponseEntity.ok(reviews);
        } catch (NoSuchElementException e) {
            // Manejar si el juego no existe
            return ResponseEntity.notFound().build();
        }
    }

    // --- POST: Crear una nueva reseña ---
    // El cuerpo de la solicitud (Review) debe incluir el ID del user y del game.
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        try {
            Review createdReview = reviewService.createReview(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
        } catch (NoSuchElementException | IllegalArgumentException e) {
            // Error si el usuario o juego no existen, o faltan IDs
            return ResponseEntity.badRequest().build();
        }
    }

    // --- PUT: Modificar una reseña existente (por el autor) ---
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review updatedReview) {
        try {
            // Aquí se debería añadir lógica de seguridad para verificar que el usuario
            // actual es el autor.
            Review result = reviewService.updateReview(id, updatedReview);
            return ResponseEntity.ok(result);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- PATCH: Moderación de reseña (Admin/Moderador) ---
    @PatchMapping("/{id}/moderate")
    // Se usa @RequestParam para recibir los parámetros del moderador
    public ResponseEntity<Review> moderateReview(@PathVariable Long id,
            @RequestParam ReviewStatus status,
            @RequestParam Long moderatorId,
            @RequestParam(required = false) String reviewNote) {
        // Lógica de seguridad para verificar rol de moderador debe ir aquí.
        try {
            // Simulamos un User para el moderador (en la práctica, se obtendría del
            // contexto de seguridad)
            Review moderatedReview = reviewService.moderateReview(id, status, moderatorId, reviewNote);
            return ResponseEntity.ok(moderatedReview);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- DELETE: Eliminar una reseña ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        try {
            // Lógica de seguridad para verificar que el usuario es el autor o un
            // administrador.
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build(); // Código 204 No Content
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}