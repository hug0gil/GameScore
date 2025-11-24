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
       // BÚSQUEDAS DE IDENTIDAD (Login y Registro)
       // ========================================================================

       Optional<User> findByEmail(String email);

       boolean existsByEmail(String email);

       // Necesario para el Login híbrido y validación de username único
       Optional<User> findByName(String name);

       Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

       // ========================================================================
       // BÚSQUEDAS POR ROL (Corregido a tipo Enum)
       // ========================================================================

       /**
        * Busca por rol exacto.
        * NOTA: Recibe 'Role', no 'String'. El servicio hace la conversión.
        */
       List<User> findByRole(Role role);

       Page<User> findByRole(Role role, Pageable pageable);

       // ========================================================================
       // BÚSQUEDAS FILTRADAS (Buscador Admin)
       // ========================================================================

       /**
        * Busca usuarios por nombre o email (case insensitive).
        */
       @Query("SELECT u FROM User u WHERE " +
                     "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       List<User> findByKeyword(@Param("keyword") String keyword);

       @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                     "OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
       Page<User> searchByEmailOrName(@Param("searchTerm") String searchTerm, Pageable pageable);

       /**
        * Busca por ROL específico + Palabra clave.
        * CORREGIDO: El parámetro 'role' es de tipo Enum Role.
        */
       @Query("SELECT u FROM User u WHERE " +
                     "u.role = :role AND (" +
                     "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
       List<User> findByRoleAndKeyword(@Param("role") Role role, @Param("keyword") String keyword);

       // ========================================================================
       // ESTADO Y PROVEEDORES
       // ========================================================================

       List<User> findByEnabled(boolean enabled);

       long countByEnabled(boolean enabled);

       List<User> findByProvider(AuthProvider provider);

       long countByProvider(AuthProvider provider);

       long countByRole(Role role);

       // ========================================================================
       // FECHAS Y ESTADÍSTICAS (Dashboard)
       // ========================================================================

       Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

       long countByCreatedAtAfter(LocalDateTime date);

       long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

       // Asumiendo que 'updatedAt' existe en tu entidad User
       long countByEnabledFalseAndUpdatedAtAfter(LocalDateTime date);

       @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
       List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       @Query("SELECT u FROM User u WHERE u.lastLogin >= :since")
       List<User> findActiveUsersSince(@Param("since") LocalDateTime since);

       Optional<User> findByResetPasswordToken(String token);

       Page<User> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

       Page<User> findByRole(String role, Pageable pageable);

       Page<User> findByRoleAndNameContainingIgnoreCase(String role, String keyword, Pageable pageable);

}