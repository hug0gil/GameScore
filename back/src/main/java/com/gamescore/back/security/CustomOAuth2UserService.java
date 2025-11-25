package com.gamescore.back.security;

import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.repository.UserRepository;
import com.gamescore.back.service.EmailService;
import com.gamescore.back.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuthPostLogin(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuthPostLogin(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = extractEmail(registrationId, attributes);
        String providerId = extractProviderId(registrationId, attributes);
        String name = extractName(registrationId, attributes);
        String avatarUrl = extractAvatarUrl(registrationId, attributes);

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // --- USUARIO EXISTE ---
            user = userOptional.get();
            log.info("Usuario existente: {}. Actualizando metadatos.", email);

            if (user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) {
                user.setAvatarUrl(avatarUrl);
            }
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

        } else {
            // --- USUARIO NUEVO ---
            log.info("Creando nuevo usuario OAuth: {}", email);

            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setAvatarUrl(avatarUrl);
            user.setEnabled(true); // OAuth siempre está verificado
            user.setLastLogin(LocalDateTime.now());

            userRepository.save(user);

            // --- ENVIAR EMAIL USANDO sendWelcomeAndConfirmation ---
            try {
                log.info("Enviando email de bienvenida (Template) a: {}", email);
                emailService.sendWelcomeAndConfirmation(email, name);
                
            } catch (Exception e) {
                log.error("Error enviando email de bienvenida", e);
            }
        }

        return new CustomOAuth2User(oAuth2User, user);
    }

    // --- MÉTODOS DE EXTRACCIÓN ---
    private String extractEmail(String provider, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        if (email == null && "github".equalsIgnoreCase(provider)) {
            String login = (String) attributes.get("login");
            email = login + "@github.local";
        }
        return email;
    }

    private String extractName(String provider, Map<String, Object> attributes) {
        String extractedName = (String) attributes.get("name");
        if (extractedName == null || extractedName.isBlank()) {
            if ("github".equalsIgnoreCase(provider)) return (String) attributes.get("login");
            if ("discord".equalsIgnoreCase(provider)) {
                String globalName = (String) attributes.get("global_name");
                return globalName != null ? globalName : (String) attributes.get("username");
            }
            String email = (String) attributes.get("email");
            if (email != null) return email.split("@")[0];
        }
        return extractedName;
    }

    private String extractProviderId(String provider, Map<String, Object> attributes) {
        Object id = attributes.get("id");
        if (id != null && "github".equalsIgnoreCase(provider)) return id.toString();
        return (String) attributes.get("sub");
    }

    private String extractAvatarUrl(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("picture");
            case "github" -> (String) attributes.get("avatar_url");
            case "discord" -> {
                String userId = (String) attributes.get("id");
                String avatarHash = (String) attributes.get("avatar");
                if (avatarHash != null) yield "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarHash + ".png";
                yield null;
            }
            default -> null;
        };
    }
}