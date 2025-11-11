-- ============================================================================
-- GAMESCORE - SISTEMA DE RESEÑAS DE VIDEOJUEGOS
-- Base de datos MySQL
-- Version: 1.0
-- ============================================================================

-- Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS gamescore 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE gamescore;

-- ============================================================================
-- LIMPIAR BASE DE DATOS (Solo para desarrollo)
-- ============================================================================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS game_platforms;
DROP TABLE IF EXISTS game_genres;
DROP TABLE IF EXISTS platforms;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS games;
DROP TABLE IF EXISTS users;

-- Eliminar vistas si existen
DROP VIEW IF EXISTS v_reviews_full;
DROP VIEW IF EXISTS v_game_stats;
DROP VIEW IF EXISTS v_user_stats;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- TABLA: USERS (Usuarios del sistema)
-- ============================================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    provider VARCHAR(20) NOT NULL CHECK (provider IN ('GOOGLE', 'GITHUB', 'DISCORD', 'LOCAL')),
    provider_id VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('ADMIN', 'USER', 'GUEST')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    
    UNIQUE KEY uk_provider_user (provider, provider_id),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_provider (provider, provider_id),
    INDEX idx_users_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Usuarios autenticados vía OAuth2';

-- ============================================================================
-- TABLA: GENRES (Géneros de videojuegos)
-- ============================================================================
CREATE TABLE genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    image_url VARCHAR(500),
    rawg_id INTEGER UNIQUE,
    
    INDEX idx_genres_slug (slug),
    INDEX idx_genres_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Géneros de videojuegos (Action, RPG, Strategy, etc.)';

-- ============================================================================
-- TABLA: PLATFORMS (Plataformas de videojuegos)
-- ============================================================================
CREATE TABLE platforms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    image_url VARCHAR(500),
    rawg_id INTEGER UNIQUE,
    
    INDEX idx_platforms_slug (slug),
    INDEX idx_platforms_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Plataformas (PC, PlayStation 5, Xbox, Switch, etc.)';

-- ============================================================================
-- TABLA: GAMES (Catálogo de videojuegos)
-- ============================================================================
CREATE TABLE games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    cover_url VARCHAR(500),
    background_url VARCHAR(500),
    release_date DATE,
    rating DECIMAL(3,2) CHECK (rating >= 0 AND rating <= 5),
    metacritic INTEGER CHECK (metacritic >= 0 AND metacritic <= 100),
    rawg_id BIGINT UNIQUE,
    youtube_key VARCHAR(100),
    website VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_games_slug (slug),
    INDEX idx_games_name (name),
    FULLTEXT INDEX idx_games_name_fulltext (name),
    INDEX idx_games_rawg_id (rawg_id),
    INDEX idx_games_release_date (release_date DESC),
    INDEX idx_games_rating (rating DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Catálogo de videojuegos importados desde RAWG API';

-- ============================================================================
-- TABLA: GAME_GENRES (Relación Many-to-Many: Juegos-Géneros)
-- ============================================================================
CREATE TABLE game_genres (
    game_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    
    PRIMARY KEY (game_id, genre_id),
    INDEX idx_game_genres_game (game_id),
    INDEX idx_game_genres_genre (genre_id),
    
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Relación entre juegos y géneros';

-- ============================================================================
-- TABLA: GAME_PLATFORMS (Relación Many-to-Many: Juegos-Plataformas)
-- ============================================================================
CREATE TABLE game_platforms (
    game_id BIGINT NOT NULL,
    platform_id BIGINT NOT NULL,
    
    PRIMARY KEY (game_id, platform_id),
    INDEX idx_game_platforms_game (game_id),
    INDEX idx_game_platforms_platform (platform_id),
    
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (platform_id) REFERENCES platforms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Relación entre juegos y plataformas';

-- ============================================================================
-- TABLA: REVIEWS (Reseñas de usuarios)
-- ============================================================================
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
    INDEX idx_reviews_user (user_id),
    INDEX idx_reviews_game (game_id),
    INDEX idx_reviews_status (status),
    INDEX idx_reviews_created (created_at DESC),
    INDEX idx_reviews_rating (rating),
    INDEX idx_reviews_approved (approved_at DESC),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    
    CONSTRAINT chk_content_length CHECK (CHAR_LENGTH(content) >= 100 AND CHAR_LENGTH(content) <= 5000),
    CONSTRAINT chk_title_length CHECK (CHAR_LENGTH(title) >= 10 AND CHAR_LENGTH(title) <= 200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Reseñas de videojuegos por usuarios';

-- ============================================================================
-- VISTAS ÚTILES
-- ============================================================================

-- Vista: Reseñas con información completa
CREATE OR REPLACE VIEW v_reviews_full AS
SELECT 
    r.id,
    r.title,
    r.content,
    r.rating,
    r.status,
    r.created_at,
    r.approved_at,
    u.id AS user_id,
    u.name AS user_name,
    u.avatar_url AS user_avatar,
    g.id AS game_id,
    g.name AS game_name,
    g.slug AS game_slug,
    g.cover_url AS game_cover,
    admin.name AS reviewed_by_name
FROM reviews r
INNER JOIN users u ON r.user_id = u.id
INNER JOIN games g ON r.game_id = g.id
LEFT JOIN users admin ON r.reviewed_by = admin.id;

-- Vista: Estadísticas de juegos
CREATE OR REPLACE VIEW v_game_stats AS
SELECT 
    g.id,
    g.name,
    g.slug,
    g.cover_url,
    g.rating AS rawg_rating,
    COUNT(r.id) AS review_count,
    ROUND(AVG(r.rating), 2) AS avg_user_rating,
    SUM(CASE WHEN r.status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_reviews
FROM games g
LEFT JOIN reviews r ON g.id = r.game_id AND r.status = 'APPROVED'
GROUP BY g.id, g.name, g.slug, g.cover_url, g.rating;

-- Vista: Estadísticas de usuarios
CREATE OR REPLACE VIEW v_user_stats AS
SELECT 
    u.id,
    u.name,
    u.email,
    u.role,
    u.created_at,
    COUNT(r.id) AS total_reviews,
    SUM(CASE WHEN r.status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_reviews,
    SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END) AS pending_reviews,
    SUM(CASE WHEN r.status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected_reviews,
    ROUND(AVG(r.rating), 2) AS avg_rating
FROM users u
LEFT JOIN reviews r ON u.id = r.user_id
GROUP BY u.id, u.name, u.email, u.role, u.created_at;
-- ============================================================================