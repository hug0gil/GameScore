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
@Table(name = "users") // Buena práctica especificar nombre de tabla
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

    @Column(nullable = false)
    private String name;

    // CAMBIO CLAVE: Campo Password añadido para usuarios locales
    // No es nullable=false porque los usuarios OAuth no tienen password
    private String password;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true; // Ojo: En registro manual se debe setear a false explícitamente

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    /**
     * Calcula el Nivel basado en la antigüedad.
     * Nivel 1 = Recién llegado.
     * Sube 1 nivel cada 30 días.
     */
    public int getLevel() {
        if (this.createdAt == null)
            return 1;

        long daysActive = ChronoUnit.DAYS.between(this.createdAt, LocalDateTime.now());
        // Dividimos los días entre 30 y sumamos 1 (para empezar en Nivel 1)
        return (int) (daysActive / 30) + 1;
    }

    /**
     * Calcula el porcentaje de la barra de experiencia (0% a 100%).
     * Representa cuánto falta para cumplir el siguiente ciclo de 30 días.
     */
    public int getXpPercent() {
        if (this.createdAt == null)
            return 0;

        long daysActive = ChronoUnit.DAYS.between(this.createdAt, LocalDateTime.now());

        // El operador módulo (%) nos da los días sobrantes del ciclo actual
        long daysIntoLevel = daysActive % 30;

        // Convertimos a porcentaje (sobre 30 días)
        return (int) ((daysIntoLevel / 30.0) * 100);
    }

    // 1. Contador de Logins
    @Column(nullable = false)
    @Builder.Default
    private Integer loginCount = 0;

    // 2. Relación con Juegos Favoritos (Asumiendo que tienes una entidad Game)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_favorites", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "game_id"))
    @Builder.Default
    private Set<Game> favorites = new HashSet<>();

    // Helper para añadir favoritos fácilmente
    public void addFavorite(Game game) {
        this.favorites.add(game);
    }

    // 3. Relación con Reviews escritas por el usuario
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}