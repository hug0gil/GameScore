package com.gamescore.back.security;

import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.repository.UserRepository;
import com.gamescore.back.service.EmailService;
import com.gamescore.back.service.UserService;
import com.mailjet.client.errors.MailjetException;

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

        // Extraer datos
        String email = extractEmail(registrationId, attributes);
        String providerId = extractProviderId(registrationId, attributes);
        String name = extractName(registrationId, attributes);
        String avatarUrl = extractAvatarUrl(registrationId, attributes);

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // --- CASO 1: EL USUARIO YA EXISTE ---
            user = userOptional.get();
            log.info("Usuario existente: {}. No se sobrescribirá el nombre.", email);

            // Solo actualizamos avatar si no tiene uno
            if (user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) {
                user.setAvatarUrl(avatarUrl);
            }

            // Actualizamos último login
            user.setLastLogin(LocalDateTime.now());

            // Guardamos cambios técnicos
            userRepository.save(user);

        } else {
            // --- CASO 2: USUARIO NUEVO ---
            log.info("Creando nuevo usuario OAuth: {}", email);

            user = new User();
            user.setEmail(email);
            user.setName(name); // Aquí SÍ usamos el nombre de Google/GitHub
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setAvatarUrl(avatarUrl);
            user.setEnabled(true);
            user.setLastLogin(LocalDateTime.now());

            userRepository.save(user);

            // Enviar email de bienvenida
            try {
                sendWelcomeEmail(email, name, registrationId);
            } catch (Exception e) {
                log.error("Error enviando email de bienvenida", e);
            }
        }

        // CORRECCIÓN FINAL: Orden (OAuth2User, User)
        return new CustomOAuth2User(oAuth2User, user);
    }

    // --- EMAIL DE BIENVENIDA ---
    private void sendWelcomeEmail(String email, String name, String provider) {
        String providerName = provider.substring(0, 1).toUpperCase() + provider.substring(1);
        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <body style="background-color: #E9E7E4; font-family: sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background: #fff; border: 4px solid #2D2D2D; box-shadow: 8px 8px 0 #F38801;">
                        <div style="background: #FFB800; padding: 20px; text-align: center; border-bottom: 4px solid #2D2D2D;">
                            <h1 style="margin:0; color:#2D2D2D;">BIENVENIDO A GAMESCORE</h1>
                        </div>
                        <div style="padding: 30px;">
                            <h2>Hola, %s!</h2>
                            <p>Tu cuenta ha sido creada conectando con <strong>%s</strong>.</p>
                            <p>Ya puedes guardar tus juegos favoritos y subir de nivel.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(name, providerName);

        emailService.sendEmail(email, name, "¡Bienvenido a GameScore! 🎮", htmlContent);
    }

    // --- MÉTODOS DE EXTRACCIÓN ---
    private String extractEmail(String provider, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");

        if (email == null && "github".equalsIgnoreCase(provider)) {
            // GitHub no devuelve email por defecto, usar login como fallback
            String login = (String) attributes.get("login");
            email = login + "@github.local"; // Esto evita que Hibernate falle
        }

        return email;
    }

    private String extractName(String provider, Map<String, Object> attributes) {
        String extractedName = (String) attributes.get("name");
        if (extractedName == null || extractedName.isBlank()) {
            if ("github".equalsIgnoreCase(provider))
                return (String) attributes.get("login");
            if ("discord".equalsIgnoreCase(provider)) {
                String globalName = (String) attributes.get("global_name");
                return globalName != null ? globalName : (String) attributes.get("username");
            }
            String email = (String) attributes.get("email");
            if (email != null)
                return email.split("@")[0];
        }
        return extractedName;
    }

    private String extractProviderId(String provider, Map<String, Object> attributes) {
        Object id = attributes.get("id");
        if (id != null && "github".equalsIgnoreCase(provider))
            return id.toString();
        return (String) attributes.get("sub");
    }

    private String extractAvatarUrl(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("picture");
            case "github" -> (String) attributes.get("avatar_url");
            case "discord" -> {
                String userId = (String) attributes.get("id");
                String avatarHash = (String) attributes.get("avatar");
                if (avatarHash != null) {
                    yield "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarHash + ".png";
                }
                yield null;
            }
            default -> null;
        };
    }
}