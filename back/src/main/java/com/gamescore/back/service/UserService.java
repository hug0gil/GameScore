package com.gamescore.back.service;

import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.model.enums.Role;
import com.gamescore.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    // ========================================================================
    // CRUD BÁSICO
    // ========================================================================
    
    /**
     * Obtiene todos los usuarios con paginación
     */
    public Page<User> findAll(Pageable pageable) {
        log.debug("Obteniendo todos los usuarios - Página: {}", pageable.getPageNumber());
        return userRepository.findAll(pageable);
    }
    
    /**
     * Busca usuario por ID
     */
    public Optional<User> findById(Long id) {
        log.debug("Buscando usuario con ID: {}", id);
        return userRepository.findById(id);
    }
    
    /**
     * Busca usuario por email
     */
    public Optional<User> findByEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    /**
     * Crea o actualiza un usuario
     */
    public User save(User user) {
        log.info("Guardando usuario: {}", user.getEmail());
        return userRepository.save(user);
    }
    
    /**
     * Elimina un usuario
     */
    public void delete(Long id) {
        log.warn("Eliminando usuario con ID: {}", id);
        userRepository.deleteById(id);
    }
    
    // ========================================================================
    // LÓGICA DE NEGOCIO
    // ========================================================================
    
    /**
     * Busca o crea usuario desde OAuth2
     * Usado en: CustomOAuth2UserService
     */
    public User findOrCreateOAuth2User(
            String email,
            String name,
            String avatarUrl,
            AuthProvider provider,
            String providerId
    ) {
        log.info("Buscando o creando usuario OAuth2: {}", email);
        
        // Buscar por proveedor y providerId primero
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        
        if (existingUser.isPresent()) {
            // Usuario existe, actualizar información
            User user = existingUser.get();
            user.setName(name);
            user.setAvatarUrl(avatarUrl);
            user.setLastLogin(LocalDateTime.now());
            
            log.debug("Usuario existente actualizado: {}", email);
            return userRepository.save(user);
        }
        
        // Si no existe por provider, buscar por email
        Optional<User> userByEmail = userRepository.findByEmail(email);
        
        if (userByEmail.isPresent()) {
            // Email existe con otro proveedor, actualizar
            User user = userByEmail.get();
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setName(name);
            user.setAvatarUrl(avatarUrl);
            user.setLastLogin(LocalDateTime.now());
            
            log.warn("Usuario encontrado por email, actualizando proveedor: {}", email);
            return userRepository.save(user);
        }
        
        // Crear nuevo usuario
        User newUser = User.builder()
                .email(email)
                .name(name)
                .avatarUrl(avatarUrl)
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER)
                .enabled(true)
                .lastLogin(LocalDateTime.now())
                .build();
        
        log.info("Nuevo usuario creado: {}", email);
        return userRepository.save(newUser);
    }
    
    /**
     * Actualiza el último login del usuario
     */
    public void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        log.debug("Último login actualizado para: {}", user.getEmail());
    }
    
    /**
     * Cambia el rol de un usuario
     * Solo ADMIN puede hacer esto
     */
    public User changeRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Role oldRole = user.getRole();
        user.setRole(newRole);
        
        log.warn("Rol cambiado para {}: {} → {}", user.getEmail(), oldRole, newRole);
        return userRepository.save(user);
    }
    
    /**
     * Activa o desactiva un usuario
     */
    public User toggleEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setEnabled(!user.getEnabled());
        
        log.warn("Usuario {} {}", 
                user.getEmail(), 
                user.getEnabled() ? "activado" : "desactivado");
        
        return userRepository.save(user);
    }
    
    // ========================================================================
    // BÚSQUEDAS ESPECIALES
    // ========================================================================
    
    /**
     * Obtiene usuarios por rol
     */
    public List<User> findByRole(Role role) {
        log.debug("Buscando usuarios con rol: {}", role);
        return userRepository.findByRole(role);
    }
    
    /**
     * Obtiene solo usuarios activos
     */
    public List<User> findActiveUsers() {
        log.debug("Obteniendo usuarios activos");
        return userRepository.findByEnabledTrue();
    }
    
    /**
     * Busca usuarios por nombre o email
     */
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Buscando usuarios con término: {}", searchTerm);
        return userRepository.searchByEmailOrName(searchTerm, pageable);
    }
    
    // ========================================================================
    // ESTADÍSTICAS
    // ========================================================================
    
    /**
     * Cuenta total de usuarios
     */
    public long countAll() {
        return userRepository.count();
    }
    
    /**
     * Cuenta usuarios por rol
     */
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }
    
    /**
     * Cuenta usuarios activos
     */
    public long countActiveUsers() {
        return userRepository.countByEnabledTrue();
    }
    
    /**
     * Cuenta usuarios por proveedor OAuth2
     */
    public long countByProvider(AuthProvider provider) {
        return userRepository.countByProvider(provider);
    }
    
    /**
     * Obtiene usuarios recientes
     */
    public Page<User> findRecentUsers(Pageable pageable) {
        log.debug("Obteniendo usuarios recientes");
        return userRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    // ========================================================================
    // VALIDACIONES
    // ========================================================================
    
    /**
     * Verifica si un email ya existe
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Verifica si el usuario puede realizar una acción
     */
    public boolean canPerformAction(User user, String action) {
        if (!user.getEnabled()) {
            log.warn("Usuario deshabilitado intentó: {}", action);
            return false;
        }
        
        return switch (action) {
            case "CREATE_REVIEW" -> user.getRole() == Role.USER || user.getRole() == Role.ADMIN;
            case "MODERATE_REVIEW" -> user.getRole() == Role.ADMIN;
            case "MANAGE_USERS" -> user.getRole() == Role.ADMIN;
            default -> false;
        };
    }
}