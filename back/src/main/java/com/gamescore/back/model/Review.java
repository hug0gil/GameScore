package com.gamescore.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.gamescore.back.model.enums.ReviewStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_game_review", columnNames = {"user_id", "game_id"})
       },
       indexes = {
           @Index(name = "idx_reviews_user", columnList = "user_id"),
           @Index(name = "idx_reviews_game", columnList = "game_id"),
           @Index(name = "idx_reviews_status", columnList = "status"),
           @Index(name = "idx_reviews_created", columnList = "created_at"),
           @Index(name = "idx_reviews_rating", columnList = "rating"),
           @Index(name = "idx_reviews_approved", columnList = "approved_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 10, max = 200, message = "El título debe tener entre 10 y 200 caracteres")
    @Column(nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 100, max = 5000, message = "El contenido debe tener entre 100 y 5000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 10, message = "La calificación máxima es 10")
    @Column(nullable = false)
    private Integer rating;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    @Column(name = "review_note", length = 1000)
    private String reviewNote;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}