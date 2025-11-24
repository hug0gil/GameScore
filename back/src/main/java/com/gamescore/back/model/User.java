package com.gamescore.back.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.model.enums.Role;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 100) // Explicito length
    private String name;

    // Agregamos length para el hash del password (BCrypt suele ser 60 chars)
    @Column(length = 255) 
    private String password;

    @Column(length = 500)
    private String avatarUrl;

    // SOLUCIÓN AL ERROR: Especificar length = 20
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) 
    private AuthProvider provider;

    @Column(length = 100)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    @Column(nullable = false)
    @Builder.Default
    private Integer loginCount = 0;

    // ... (El resto de métodos getLevel, getXpPercent y relaciones se mantienen igual)
    
    public int getLevel() {
        if (this.createdAt == null) return 1;
        long daysActive = ChronoUnit.DAYS.between(this.createdAt, LocalDateTime.now());
        return (int) (daysActive / 30) + 1;
    }

    public int getXpPercent() {
        if (this.createdAt == null) return 0;
        long daysActive = ChronoUnit.DAYS.between(this.createdAt, LocalDateTime.now());
        long daysIntoLevel = daysActive % 30;
        return (int) ((daysIntoLevel / 30.0) * 100);
    }
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_favorites", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "game_id"))
    @Builder.Default
    private Set<Game> favorites = new HashSet<>();

    public void addFavorite(Game game) { this.favorites.add(game); }

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}