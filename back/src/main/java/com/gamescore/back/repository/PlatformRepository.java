package com.gamescore.back.repository;

import com.gamescore.back.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {
    
    // ========================================================================
    // BÚSQUEDAS BÁSICAS
    // ========================================================================
    
    /**
     * Busca plataforma por slug
     * Usado en: URLs amigables (/platforms/playstation-5)
     */
    Optional<Platform> findBySlug(String slug);
    
    /**
     * Busca plataforma por nombre
     * Usado en: Verificar si existe antes de crear
     */
    Optional<Platform> findByName(String name);
    
    /**
     * Busca plataforma por ID de RAWG
     * Usado en: Importar desde RAWG API
     */
    Optional<Platform> findByRawgId(Integer rawgId);
    
    /**
     * Verifica si existe una plataforma por nombre
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
     * Plataformas ordenadas por nombre
     * Usado en: Dropdown de selección en formularios
     */
    List<Platform> findAllByOrderByNameAsc();
    
    /**
     * Busca plataformas por nombre (case insensitive, parcial)
     * Usado en: Autocomplete al buscar plataformas
     */
    @Query("SELECT p FROM Platform p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Platform> searchByName(String searchTerm);
}