package com.gamescore.back.repository;

import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ========================================================================
    // MÉTODOS DE BÚSQUEDA BÁSICOS
    // ========================================================================
    
    /**
     * Busca un usuario por email
     * Usado en: Login, registro, verificación de duplicados
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Busca usuario por proveedor OAuth2 y su ID
     * Usado en: OAuth2 login (evitar crear usuarios duplicados)
     */
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    
    /**
     * Verifica si existe un email
     * Usado en: Validación antes de crear usuario
     */
    boolean existsByEmail(String email);
    
    // ========================================================================
    // BÚSQUEDAS POR ROL Y ESTADO
    // ========================================================================
    
    /**
     * Obtiene todos los usuarios de un rol específico
     * Usado en: Panel admin (listar admins, users, guests)
     */
    List<User> findByRole(Role role);
    
    /**
     * Obtiene usuarios activos y desactivados
     * Usado en: Estadísticas de usuarios activos y Panel admin (usuarios bloqueados)
     */
    List<User> findByEnabled(boolean enabled);
    
    /**
     * Busca usuarios por rol con paginación
     * Usado en: Panel admin (tabla de usuarios por rol)
     */
    Page<User> findByRole(Role role, Pageable pageable);
    
    // ========================================================================
    // BÚSQUEDAS POR PROVEEDOR
    // ========================================================================
    
    /**
     * Cuenta usuarios por proveedor OAuth2
     * Usado en: Estadísticas (cuántos usan Google vs GitHub vs Discord)
     */
    long countByProvider(AuthProvider provider);
    
    /**
     * Obtiene usuarios de un proveedor específico
     * Usado en: Análisis de proveedores más usados
     */
    List<User> findByProvider(AuthProvider provider);
    
    // ========================================================================
    // BÚSQUEDAS POR FECHA
    // ========================================================================
    
    /**
     * Usuarios registrados en un rango de fechas
     * Usado en: Gráficos de nuevos usuarios por mes
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Usuarios que hicieron login recientemente
     * Usado en: Usuarios activos en las últimas 24h
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :since")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);
    
    // ========================================================================
    // ESTADÍSTICAS
    // ========================================================================
    
    /**
     * Cuenta usuarios por rol
     * Usado en: Dashboard admin (torta de distribución de roles)
     */
    long countByRole(Role role);
    
    /**
     * Cuenta usuarios activos
     * Usado en: Estadísticas generales
     */
    long countByEnabled(boolean enabled);
    
    /**
     * Obtiene los N usuarios más recientes
     * Usado en: Panel admin (últimos registros)
     */
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // ========================================================================
    // BÚSQUEDAS AVANZADAS
    // ========================================================================
    
    /**
     * Busca usuarios por nombre (case insensitive)
     * Usado en: Buscador del panel admin
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Busca usuarios por email o nombre
     * Usado en: Buscador avanzado del panel admin
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchByEmailOrName(@Param("searchTerm") String searchTerm, Pageable pageable);

    
    // ========================================================================
    // MÉTODOS AÑADIDOS PARA EL DASHBOARD
    // ========================================================================

    /**
     * Cuenta usuarios creados después de una fecha específica.
     * Usado en: Tarjeta "Users Nuevos".
     */
    long countByCreatedAtAfter(LocalDateTime date);

    /**
     * Cuenta usuarios creados en un rango de fechas.
     * Usado en: Gráfico de nuevos usuarios. Es más eficiente que findByCreatedAtBetween().
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Cuenta usuarios cuyo estado cambió a 'desactivado' (enabled=false)
     * después de una fecha específica. Asumimos que 'updatedAt' se actualiza
     * al cambiar el estado.
     * Usado en: Tarjeta "Users Baja".
     */
    long countByEnabledFalseAndUpdatedAtAfter(LocalDateTime date);
}