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

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    // Usamos 'final' para que @RequiredArgsConstructor haga la inyección automáticamente
    private final UserService userService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // --- LOGS DE DEPURACIÓN ---
        log.info("--- ATRIBUTOS RECIBIDOS ({}) ---", registrationId.toUpperCase());
        // attributes.forEach((key, value) -> log.info("[{}]: {}", key, value)); // Descomentar si necesitas ver todo
        
        // Extraer información
        String email = extractEmail(registrationId, attributes);
        String name = extractName(registrationId, attributes);
        String avatarUrl = extractAvatarUrl(registrationId, attributes);
        String providerId = extractProviderId(registrationId, attributes);
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        if (email == null) {
            log.error("Email nulo de {}. Revisa scopes.", registrationId);
            throw new OAuth2AuthenticationException("Email no encontrado en respuesta OAuth2.");
        }

        // ---------------------------------------------------------------
        // LÓGICA DE BIENVENIDA: Verificamos si existe ANTES de llamar al servicio
        // ---------------------------------------------------------------
        boolean isNewUser = userRepository.findByEmail(email).isEmpty();

        // Buscar o crear usuario (Lógica existente)
        User user = userService.findOrCreateOAuth2User(
            email, name, avatarUrl, provider, providerId
        );
        
        // Si detectamos que era nuevo, enviamos el correo
        if (isNewUser) {
            log.info("¡Nuevo usuario detectado! Enviando correo de bienvenida a: {}", email);
            try {
                sendWelcomeEmail(email, name, registrationId);
            } catch (MailjetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            log.info("Usuario existente logueado: {}", email);
        }
        
        return new CustomOAuth2User(oauth2User, user);
    }

    // --- MÉTODO PRIVADO PARA ENVIAR EL EMAIL ---
    private void sendWelcomeEmail(String email, String name, String provider) throws MailjetException {
        
        // Capitalizar el proveedor (google -> Google)
        String providerName = provider.substring(0, 1).toUpperCase() + provider.substring(1);

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&family=Roboto&display=swap" rel="stylesheet">
            </head>
            <body style="margin: 0; padding: 0; background-color: #E9E7E4;">
                <div style="
                    font-family: 'Roboto', 'Helvetica', Arial, sans-serif; 
                    background-color: #E9E7E4; 
                    padding: 40px 20px;
                ">
                    <!-- TARJETA PRINCIPAL CON BORDE Y SOMBRA RETRO -->
                    <div style="
                        max-width: 600px; 
                        margin: 0 auto; 
                        background-color: #FFFFFF; 
                        border: 3px solid #2D2D2D; 
                        box-shadow: 8px 8px 0px #F38801; 
                        padding: 0;
                        overflow: hidden;
                    ">
                        
                        <!-- CABECERA -->
                        <div style="
                            background-color: #FFB800; 
                            padding: 20px; 
                            border-bottom: 3px solid #2D2D2D;
                            text-align: center;
                        ">
                            <h1 style="
                                margin: 0; 
                                color: #2D2D2D; 
                                font-family: 'Press Start 2P', 'Courier New', monospace; 
                                font-size: 20px; 
                                text-transform: uppercase;
                                line-height: 1.5;
                            ">
                                ¡Bienvenido a GameScore! 🎮
                            </h1>
                        </div>

                        <!-- CONTENIDO -->
                        <div style="padding: 30px;">
                            <h2 style="color: #333333; margin-top: 0;">Hola, %s</h2>
                            
                            <p style="color: #333333; font-size: 16px; line-height: 1.6;">
                                Estamos muy contentos de que te hayas unido a nuestra comunidad de jugadores.
                                Tu cuenta ha sido creada exitosamente vinculando tu cuenta de 
                                <strong style="
                                    color: #2D2D2D; 
                                    background-color: #FFB800; 
                                    padding: 2px 6px; 
                                    border: 1px solid #2D2D2D;
                                    border-radius: 4px;
                                ">%s</strong>.
                            </p>

                            <!-- LISTA DE ACCIONES -->
                            <div style="
                                background-color: #F9F9F9; 
                                border-left: 5px solid #55B957; 
                                padding: 15px; 
                                margin: 25px 0;
                            ">
                                <p style="margin: 0 0 10px 0; font-weight: bold; color: #2D2D2D;">
                                    ¿Qué puedes hacer ahora?
                                </p>
                                <ul style="color: #333333; padding-left: 20px; margin-bottom: 0;">
                                    <li style="margin-bottom: 8px;">Explorar el catálogo de juegos.</li>
                                    <li style="margin-bottom: 8px;">Guardar tus favoritos.</li>
                                    <li>Ver tus estadísticas detalladas.</li>
                                </ul>
                            </div>

                            <p style="color: #333333; font-size: 16px;">
                                ¡A jugar!
                            </p>
                            
                            <p style="
                                font-family: 'Press Start 2P', 'Courier New', monospace; 
                                font-size: 12px; 
                                color: #888888; 
                                margin-top: 30px;
                            ">
                                El equipo de GameScore
                            </p>
                        </div>
                        
                        <!-- FOOTER LINEA -->
                        <div style="height: 8px; background-color: #2D2D2D;"></div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, providerName);

        emailService.sendEmail(email, name, "¡Bienvenido a GameScore! 🎮", htmlContent);
    }
    
    // --- MÉTODOS DE EXTRACCIÓN (MANTENIDOS IGUAL) ---

    private String extractEmail(String provider, Map<String, Object> attributes) {
        return (String) attributes.get("email");
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
                if (avatarHash != null) {
                    yield "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarHash + ".png";
                }
                yield null;
            }
            default -> null;
        };
    }
}