-- ============================================================================
-- GAMESCORE - SISTEMA DE RESEÑAS DE VIDEOJUEGOS
-- Script Corregido para soporte LOCAL y OAuth2
-- ============================================================================

-- 1. CONFIGURACIÓN INICIAL
CREATE DATABASE IF NOT EXISTS gamescore 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE gamescore;

-- Desactivar checks para limpiar tablas sin errores
SET FOREIGN_KEY_CHECKS = 0;

-- 2. LIMPIEZA (Orden inverso de dependencias)
DROP VIEW IF EXISTS v_user_stats;
DROP VIEW IF EXISTS v_game_stats;
DROP VIEW IF EXISTS v_reviews_full;

DROP TABLE IF EXISTS user_favorites; -- Faltaba en tu script anterior
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS game_platforms;
DROP TABLE IF EXISTS game_genres;
DROP TABLE IF EXISTS platforms;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS games;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 3. CREACIÓN DE TABLAS
-- ============================================================================

-- TABLA: USERS
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(255),             -- AÑADIDO: Necesario para Auth Local
    avatar_url VARCHAR(500),
    
    -- CORREGIDO: VARCHAR(20) y añadido 'LOCAL' al Check
    provider VARCHAR(20) NOT NULL CHECK (provider IN ('GOOGLE', 'GITHUB', 'DISCORD', 'LOCAL')),
    provider_id VARCHAR(100),
    
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('ADMIN', 'USER', 'GUEST')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    
    login_count INTEGER NOT NULL DEFAULT 0, -- AÑADIDO: Coincide con tu entidad Java
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    
    UNIQUE KEY uk_provider_user (provider, provider_id),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA: GENRES
CREATE TABLE genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    image_url VARCHAR(500),
    rawg_id INTEGER UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA: PLATFORMS
CREATE TABLE platforms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    image_url VARCHAR(500),
    rawg_id INTEGER UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA: GAMES
CREATE TABLE games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    cover_url VARCHAR(500),
    background_url VARCHAR(500),
    release_date DATE,
    rating DECIMAL(3,2),
    metacritic INTEGER,
    rawg_id BIGINT UNIQUE,
    youtube_key VARCHAR(100),
    website VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FULLTEXT INDEX idx_games_name_fulltext (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- RELACIÓN: GAME_GENRES
CREATE TABLE game_genres (
    game_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (game_id, genre_id),
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- RELACIÓN: GAME_PLATFORMS
CREATE TABLE game_platforms (
    game_id BIGINT NOT NULL,
    platform_id BIGINT NOT NULL,
    PRIMARY KEY (game_id, platform_id),
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (platform_id) REFERENCES platforms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- RELACIÓN: USER_FAVORITES (Añadida para soportar @ManyToMany favorites)
CREATE TABLE user_favorites (
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, game_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA: REVIEWS
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 10),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    reviewed_by BIGINT,
    review_note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL,
    
    UNIQUE KEY uk_user_game_review (user_id, game_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4. VISTAS (VIEWS)
-- ============================================================================

-- Vista: Reseñas completas
CREATE OR REPLACE VIEW v_reviews_full AS
SELECT 
    r.id, r.title, r.content, r.rating, r.status, r.created_at, r.approved_at,
    u.id AS user_id, u.name AS user_name, u.avatar_url AS user_avatar,
    g.id AS game_id, g.name AS game_name, g.slug AS game_slug, g.cover_url AS game_cover
FROM reviews r
JOIN users u ON r.user_id = u.id
JOIN games g ON r.game_id = g.id;

-- Vista: Estadísticas de Juegos
CREATE OR REPLACE VIEW v_game_stats AS
SELECT 
    g.id, g.name, g.slug, g.cover_url, g.rating AS rawg_rating,
    COUNT(r.id) AS review_count,
    ROUND(AVG(r.rating), 2) AS avg_user_rating
FROM games g
LEFT JOIN reviews r ON g.id = r.game_id AND r.status = 'APPROVED'
GROUP BY g.id;

-- Vista: Estadísticas de Usuarios
CREATE OR REPLACE VIEW v_user_stats AS
SELECT 
    u.id, u.name, u.email, u.role, u.created_at,
    COUNT(r.id) AS total_reviews,
    SUM(CASE WHEN r.status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_reviews
FROM users u
LEFT JOIN reviews r ON u.id = r.user_id
GROUP BY u.id;