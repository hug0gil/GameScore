package com.gamescore.back.service;

import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.model.enums.Role;
import com.gamescore.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // Inyecciones específicas para la funcionalidad de registro (Del Código 1)
    @Autowired
    private EmailService emailService;

    @Value("${app.base.url}")
    private String baseUrl;

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
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));
    }

    /**
     * Busca un usuario por su nombre de usuario (email).
     * Método wrapper útil para integración con Spring Security o validaciones.
     * (Funcionalidad extraída del Código 2)
     */
    public Optional<User> findByUsername(String username) {
        log.debug("Buscando usuario por username/email: {}", username);
        return userRepository.findByEmail(username);
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
     */
    @Transactional
    public User findOrCreateOAuth2User(
            String email,
            String name,
            String avatarUrl,
            AuthProvider provider,
            String providerId) {
        log.info("Buscando o creando usuario OAuth2: {}", email);

        Optional<User> existingUserByProvider = userRepository.findByProviderAndProviderId(provider, providerId);

        if (existingUserByProvider.isPresent()) {
            User user = existingUserByProvider.get();
            log.debug("Usuario existente encontrado por proveedor: {}. Actualizando...", email);

            user.setName(name);
            user.setAvatarUrl(avatarUrl);
            user.setLastLogin(LocalDateTime.now());

            if (user.getRole() == null) {
                log.warn("Usuario {} encontrado con rol nulo. Asignando rol USER por defecto.", user.getEmail());
                user.setRole(Role.USER);
            }

            return userRepository.save(user);
        }

        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            log.warn("Usuario encontrado por email con otro proveedor. Actualizando proveedor a {}", provider);

            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setName(name);
            user.setAvatarUrl(avatarUrl);
            user.setLastLogin(LocalDateTime.now());

            if (user.getRole() == null) {
                log.warn("Usuario {} encontrado con rol nulo. Asignando rol USER por defecto.", user.getEmail());
                user.setRole(Role.USER);
            }

            return userRepository.save(user);
        }

        log.info("Nuevo usuario no encontrado por proveedor ni email. Creando: {}", email);
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

    /**
     * Registra un usuario y envía correo de confirmación.
     * (Funcionalidad extraída del Código 1)
     */
    public void registerUser(String email, String name) {
        // 1. Lógica para guardar usuario en DB...
        
        // 2. Generar token
        String token = UUID.randomUUID().toString();
        // TODO: Guardar token en DB asociado al usuario (TokenRepository)

        // 3. Construir link y HTML
        String confirmLink = baseUrl + "/api/auth/confirm?token=" + token;
        
        String html = "<h1>¡Hola " + name + "!</h1>"
                    + "<p>Bienvenido. Confirma tu cuenta aquí:</p>"
                    + "<a href='" + confirmLink + "'>Confirmar Cuenta</a>";

        // 4. Enviar
        try {
            emailService.sendEmail(email, name, "Bienvenido - Confirma tu cuenta", html);
        } catch (Exception e) {
            log.error("Error enviando email de confirmación a {}: {}", email, e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================================================
    // BÚSQUEDAS ESPECIALES
    // ========================================================================

    public List<User> findByRole(Role role) {
        log.debug("Buscando usuarios con rol: {}", role);
        return userRepository.findByRole(role);
    }

    public List<User> findActiveUsers() {
        log.debug("Obteniendo usuarios activos");
        return userRepository.findByEnabled(true);
    }

    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Buscando usuarios con término: {}", searchTerm);
        return userRepository.searchByEmailOrName(searchTerm, pageable);
    }

    // ========================================================================
    // ESTADÍSTICAS
    // ========================================================================

    public long countAll() {
        return userRepository.count();
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    public long countActiveUsers() {
        return userRepository.countByEnabled(true);
    }

    public long countByProvider(AuthProvider provider) {
        return userRepository.countByProvider(provider);
    }

    public Page<User> findRecentUsers(Pageable pageable) {
        log.debug("Obteniendo usuarios recientes");
        return userRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // ========================================================================
    // VALIDACIONES
    // ========================================================================

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

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