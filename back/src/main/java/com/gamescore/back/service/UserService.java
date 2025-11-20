package com.gamescore.back.service;

import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.model.enums.Role;
import com.gamescore.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
     * Busca un usuario por su email. Lanza una excepción si no se encuentra.
     * Esencial para obtener los datos del usuario logueado.
     *
     * @param email El email del usuario a buscar.
     * @return El objeto User encontrado.
     * @throws UsernameNotFoundException si el usuario no existe.
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));
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
    @Transactional
    public User findOrCreateOAuth2User(
            String email,
            String name,
            String avatarUrl,
            AuthProvider provider,
            String providerId) {
        log.info("Buscando o creando usuario OAuth2: {}", email);

        // Tu lógica de búsqueda por proveedor y providerId es buena, la mantenemos.
        Optional<User> existingUserByProvider = userRepository.findByProviderAndProviderId(provider, providerId);

        if (existingUserByProvider.isPresent()) {
            // --- BLOQUE DE ACTUALIZACIÓN MEJORADO ---
            User user = existingUserByProvider.get();
            log.debug("Usuario existente encontrado por proveedor: {}. Actualizando...", email);

            user.setName(name);
            user.setAvatarUrl(avatarUrl);
            user.setLastLogin(LocalDateTime.now());

            // ¡CÓDIGO DEFENSIVO! Si el rol es nulo por alguna razón, lo arreglamos.
            if (user.getRole() == null) {
                log.warn("Usuario {} encontrado con rol nulo. Asignando rol USER por defecto.", user.getEmail());
                user.setRole(Role.USER);
            }

            return userRepository.save(user);
        }

        // Si no, buscar por email (tu lógica también es buena aquí)
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            // --- BLOQUE DE ACTUALIZACIÓN MEJORADO (TAMBIÉN AQUÍ) ---
            User user = userByEmail.get();
            log.warn("Usuario encontrado por email con otro proveedor. Actualizando proveedor a {}", provider);

            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setName(name);
            user.setAvatarUrl(avatarUrl);
            user.setLastLogin(LocalDateTime.now());

            // ¡CÓDIGO DEFENSIVO! Arreglamos el rol nulo también en este caso.
            if (user.getRole() == null) {
                log.warn("Usuario {} encontrado con rol nulo. Asignando rol USER por defecto.", user.getEmail());
                user.setRole(Role.USER);
            }

            return userRepository.save(user);
        }

        // Si no existe de ninguna manera, crear nuevo usuario (tu lógica es correcta)
        log.info("Nuevo usuario no encontrado por proveedor ni email. Creando: {}", email);
        User newUser = User.builder()
                .email(email)
                .name(name)
                .avatarUrl(avatarUrl)
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER) // Aseguramos que se establece en la creación
                .enabled(true)
                .lastLogin(LocalDateTime.now())
                .build();

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
        return userRepository.findByEnabled(true);
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
        return userRepository.countByEnabled(true);
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

    public Object findAllFilteredByRole(String role) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAllFilteredByRole'");
    }
}