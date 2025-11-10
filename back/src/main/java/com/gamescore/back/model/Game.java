package com.gamescore.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "games",
       indexes = {
           @Index(name = "idx_games_slug", columnList = "slug"),
           @Index(name = "idx_games_name", columnList = "name"),
           @Index(name = "idx_games_rawg_id", columnList = "rawg_id"),
           @Index(name = "idx_games_release_date", columnList = "release_date"),
           @Index(name = "idx_games_rating", columnList = "rating")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre del juego es obligatorio")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(nullable = false, unique = true, length = 255)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "cover_url", length = 500)
    private String coverUrl;
    
    @Column(name = "background_url", length = 500)
    private String backgroundUrl;
    
    @Column(name = "release_date")
    private LocalDate releaseDate;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;
    
    @Min(0)
    @Max(100)
    private Integer metacritic;
    
    @Column(name = "rawg_id", unique = true)
    private Long rawgId;
    
    @Column(name = "youtube_key", length = 100)
    private String youtubeKey;
    
    @Column(length = 500)
    private String website;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "game_genres",
        joinColumns = @JoinColumn(name = "game_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "game_platforms",
        joinColumns = @JoinColumn(name = "game_id"),
        inverseJoinColumns = @JoinColumn(name = "platform_id")
    )
    @Builder.Default
    private Set<Platform> platforms = new HashSet<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}