package com.gamescore.back.service;

import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.model.enums.Role;
import com.gamescore.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Override
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {
        User user = findByEmailOrUsername(loginInput)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email o username: " + loginInput));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                user.getEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }

    // ========================================================================
    // REGISTRO LOCAL
    // ========================================================================

    @Transactional
    public User registerUser(User user) {
        log.info("Registrando nuevo usuario local: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        if (user.getName() != null && !user.getName().isEmpty()) {
            // Asumiendo que tienes existsByName en tu repositorio o similar
            // Si no tienes el método en repo, podrías omitir esta validación o añadirla
             // if (userRepository.findByName(user.getName()).isPresent()) { ... }
        } else {
            user.setName(user.getEmail().split("@")[0]);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        sendConfirmationEmail(savedUser.getEmail(), savedUser.getName());

        return savedUser;
    }

    // ========================================================================
    // MÉTODOS DE BÚSQUEDA
    // ========================================================================

    public Optional<User> findByEmailOrUsername(String value) {
        Optional<User> byEmail = userRepository.findByEmail(value);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        return userRepository.findByName(value);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByName(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    // ========================================================================
    // OAUTH2
    // ========================================================================

    @Transactional
    public User findOrCreateOAuth2User(String email, String name, String avatarUrl, AuthProvider provider,
            String providerId) {
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
        user.setAvatarUrl(avatarUrl);
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    // ========================================================================
    // CRUD & ADMIN
    // ========================================================================

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

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
        if (!user.getEnabled())
            return false;
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
        return userRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // ========================================================================
    // PRIVATE
    // ========================================================================

    private void sendConfirmationEmail(String email, String name) {
        try {
            // Token simple para la URL (aunque no se valide estrictamente en esta versión simplificada)
            String token = UUID.randomUUID().toString();
            // Usamos el método genérico o el específico si lo tienes
            // Aquí uso el genérico como fallback si no tienes el template específico
            String html = "<h1>¡Hola " + name + "!</h1><p>Bienvenido a GameScore.</p>";
            emailService.sendEmail(email, name, "Bienvenido a GameScore", html);
        } catch (Exception e) {
            log.error("Error enviando email: {}", e.getMessage());
        }
    }

    public List<User> findAllFilteredByRole(String roleName) {
        try {
            Role role = Role.valueOf(roleName.toUpperCase());
            return userRepository.findByRole(role);
        } catch (IllegalArgumentException e) {
            log.warn("Se intentó buscar por un rol inválido: {}", roleName);
            return Collections.emptyList();
        }
    }

    // ========================================================================
    // PAGINACIÓN ADICIONAL
    // ========================================================================

    public Page<User> findAllPaged(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return userRepository.findAll(pageable);
    }

    public Page<User> searchPagedByKeyword(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return userRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    public Page<User> findAllPagedByRole(String role, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        // Nota: Asegúrate de que tu repositorio acepte String en findByRole 
        // o convierte 'role' a Enum aquí antes de llamar.
        return userRepository.findByRole(role, pageable);
    }

    public Page<User> searchPagedByRoleAndKeyword(String role, String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return userRepository.findByRoleAndNameContainingIgnoreCase(role, keyword, pageable);
    }
}