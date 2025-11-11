package com.gamescore.back.repository;

import com.gamescore.back.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    
    // ========================================================================
    // BÚSQUEDAS BÁSICAS
    // ========================================================================
    
    /**
     * Busca género por slug
     * Usado en: URLs amigables (/genres/action)
     */
    Optional<Genre> findBySlug(String slug);
    
    /**
     * Busca género por nombre
     * Usado en: Verificar si existe antes de crear
     */
    Optional<Genre> findByName(String name);
    
    /**
     * Busca género por ID de RAWG
     * Usado en: Importar desde RAWG API
     */
    Optional<Genre> findByRawgId(Integer rawgId);
    
    /**
     * Verifica si existe un género por nombre
     * Usado en: Validación antes de crear
     */
    boolean existsByName(String name);
    
    /**
     * Verifica si existe por slug
     * Usado en: Validación de URLs únicas
     */
    boolean existsBySlug(String slug);
    
    // ========================================================================
    // BÚSQUEDAS AVANZADAS
    // ========================================================================
    
    /**
     * Géneros ordenados por nombre
     * Usado en: Dropdown de selección en formularios
     */
    List<Genre> findAllByOrderByNameAsc();
    
    /**
     * Busca géneros por nombre (case insensitive, parcial)
     * Usado en: Autocomplete al buscar géneros
     */
    @Query("SELECT g FROM Genre g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Genre> searchByName(String searchTerm);
}