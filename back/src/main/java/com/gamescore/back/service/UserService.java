package com.gamescore.back.service;

import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.model.enums.Role;
import com.gamescore.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.base.url}")
    private String baseUrl;

    // ========================================================================
    // SPRING SECURITY (Login Híbrido: Email o Username)
    // ========================================================================

    /**
     * Permite el login usando Email O Username.
     * Spring Security llama a este método con lo que el usuario escriba en el login.
     */
    @Override
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {
        // Buscamos por Email O por Nombre (Username)
        User user = findByEmailOrUsername(loginInput)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email o username: " + loginInput));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Spring usa esto como identificador principal en la sesión
                user.getPassword() != null ? user.getPassword() : "",
                user.getEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    // ========================================================================
    // REGISTRO LOCAL
    // ========================================================================

    @Transactional
    public User registerUser(User user) {
        log.info("Registrando nuevo usuario local: {}", user.getEmail());

        // 1. Validación estricta: Ni el email ni el username pueden estar repetidos
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }
        
        // Validamos el Username (campo name) si viene informado
        if (user.getName() != null && !user.getName().isEmpty()) {
            // Asumiendo que tienes existsByName en tu repositorio
            if (userRepository.findByName(user.getName()).isPresent()) { 
                throw new IllegalArgumentException("El nombre de usuario (username) ya está en uso.");
            }
        } else {
            // Si no puso nombre, generamos uno por defecto desde el email
            user.setName(user.getEmail().split("@")[0]);
        }

        // 2. Configuración
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // 3. Email
        sendConfirmationEmail(savedUser.getEmail(), savedUser.getName());

        return savedUser;
    }

    // ========================================================================
    // MÉTODOS DE BÚSQUEDA (Recuperados y Mejorados)
    // ========================================================================

    /**
     * Método auxiliar clave para el Login Híbrido.
     * Intenta encontrar usuario por Email, si no, busca por Username (Name).
     */
    public Optional<User> findByEmailOrUsername(String value) {
        // 1. Intento directo por email
        Optional<User> byEmail = userRepository.findByEmail(value);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        // 2. Si falla, intento por username (campo name)
        return userRepository.findByName(value);
    }

    /**
     * Recuperado explícitamente por si necesitas buscar SOLO por username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByName(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    // ========================================================================
    // OAUTH2 (Sin cambios mayores, pero usando name correctamente)
    // ========================================================================

    @Transactional
    public User findOrCreateOAuth2User(String email, String name, String avatarUrl, AuthProvider provider, String providerId) {
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        if (existingUser.isPresent()) {
            return updateUserLoginStats(existingUser.get(), name, avatarUrl);
        }

        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            user.setProvider(provider);
            user.setProviderId(providerId);
            return updateUserLoginStats(user, name, avatarUrl);
        }
        
        // NOTA: En OAuth2 el nombre puede venir repetido. 
        // Si tu base de datos tiene restricción UNIQUE en 'name', aquí deberías 
        // agregar lógica para añadir un sufijo aleatorio si el nombre ya existe.

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

    private User updateUserLoginStats(User user, String name, String avatarUrl) {
        // Opcional: Puedes decidir NO sobrescribir el nombre si el usuario ya lo cambió localmente
        // user.setName(name); 
        user.setAvatarUrl(avatarUrl);
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    // ========================================================================
    // CRUD & ADMIN (Igual que antes)
    // ========================================================================

    public Page<User> findAll(Pageable pageable) { return userRepository.findAll(pageable); }
    public List<User> findAll() { return userRepository.findAll(); }
    public Optional<User> findById(Long id) { return userRepository.findById(id); }
    
    public User save(User user) { return userRepository.save(user); }
    
    public void delete(Long id) { userRepository.deleteById(id); }

    public User changeRole(Long userId, Role newRole) {
        User user = findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public User toggleEnabled(Long userId) {
        User user = findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setEnabled(!user.getEnabled());
        return userRepository.save(user);
    }

    public boolean canPerformAction(User user, String action) {
        if (!user.getEnabled()) return false;
        return switch (action) {
            case "CREATE_REVIEW" -> user.getRole() == Role.USER || user.getRole() == Role.ADMIN;
            case "MODERATE_REVIEW", "MANAGE_USERS" -> user.getRole() == Role.ADMIN;
            default -> false;
        };
    }
    
    // Estadísticas y Búsquedas avanzadas
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchByEmailOrName(searchTerm, pageable);
    }
        public List<User> findByRoleAndKeyword(String roleName, String keyword) {
        try {
            Role role = Role.valueOf(roleName.toUpperCase());
            return userRepository.findByRoleAndKeyword(role, keyword);
        } catch (IllegalArgumentException e) {
            log.warn("Rol inválido en búsqueda: {}", roleName);
            return Collections.emptyList();
        }
    }
    public List<User> findByKeyword(String keyword) {
        return userRepository.findByKeyword(keyword);
    }
    public long countAll() { return userRepository.count(); }
    public long countByRole(Role role) { return userRepository.countByRole(role); }
    public long countActiveUsers() { return userRepository.countByEnabled(true); }
    public long countByProvider(AuthProvider provider) { return userRepository.countByProvider(provider); }
    public Page<User> findRecentUsers(Pageable pageable) { return userRepository.findAllByOrderByCreatedAtDesc(pageable); }

    // ========================================================================
    // PRIVATE
    // ========================================================================
    
    private void sendConfirmationEmail(String email, String name) {
        try {
            String token = UUID.randomUUID().toString();
            String confirmLink = baseUrl + "/api/auth/confirm?token=" + token;
            String html = "<h1>¡Hola " + name + "!</h1><p>Bienvenido.</p>";
            emailService.sendEmail(email, name, "Bienvenido a GameScore", html);
        } catch (Exception e) {
            log.error("Error enviando email: {}", e.getMessage());
        }
    }

        /**
     * Busca usuarios por rol recibiendo un String.
     * Convierte el String al Enum Role antes de buscar.
     */
    public List<User> findAllFilteredByRole(String roleName) {
        try {
            // Convertimos el String (ej: "admin" o "ADMIN") al Enum (Role.ADMIN)
            Role role = Role.valueOf(roleName.toUpperCase());
            return userRepository.findByRole(role);
        } catch (IllegalArgumentException e) {
            // Si envían un rol que no existe (ej: "SUPERUSER"), devolvemos lista vacía
            log.warn("Se intentó buscar por un rol inválido: {}", roleName);
            return Collections.emptyList();
        }
    }

        // 1. SOLICITAR RESET (Genera token y envía email)
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos un usuario con ese correo."));

        // VALIDACIÓN CRUCIAL: Solo usuarios LOCAL pueden cambiar password aquí
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new IllegalArgumentException("Esta cuenta está vinculada con " + user.getProvider() + 
                                             ". Debes cambiar la contraseña en esa plataforma.");
        }

        // Generar token
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        // El token expira en 30 minutos
        user.setTokenExpirationDate(LocalDateTime.now().plusMinutes(30)); 
        
        userRepository.save(user);

        // Enviar Email
        String resetLink = baseUrl + "/cambiar-password?token=" + token;
        String htmlContent = "<h1>Recuperación de Contraseña</h1>"
                + "<p>Hola " + user.getName() + ", has solicitado restablecer tu contraseña.</p>"
                + "<p>Haz clic en el siguiente botón para continuar (válido por 30 min):</p>"
                + "<a href=\"" + resetLink + "\" style=\"padding:10px 20px; background-color:#4CAF50; color:white; text-decoration:none;\">Restablecer Contraseña</a>"
                + "<p>Si no fuiste tú, ignora este mensaje.</p>";

        emailService.sendEmail(user.getEmail(), user.getName(), "Restablecer contraseña - GameScore", htmlContent);
    }

    // 2. VALIDAR TOKEN (Para mostrar el formulario)
    public User getByResetToken(String token) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido."));

        if (user.getTokenExpirationDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El enlace ha expirado. Solicita uno nuevo.");
        }
        return user;
    }

    // 3. ACTUALIZAR CONTRASEÑA
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null); // Limpiamos el token para que no se use de nuevo
        user.setTokenExpirationDate(null);
        userRepository.save(user);
    }
}