package com.gamescore.back.security;

import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.service.UserService;
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
    
    private final UserService userService;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // --- LOGS DE DEPURACIÓN ---
        log.info("--- ATRIBUTOS CRUDOS RECIBIDOS DEL PROVEEDOR: {} ---", registrationId.toUpperCase());
        attributes.forEach((key, value) -> log.info("Atributo: [{}], Valor: [{}]", key, value));
        log.info("---------------------------------------------------------");
        
        // Extraer información de forma segura
        String email = extractEmail(registrationId, attributes);
        String name = extractName(registrationId, attributes);
        String avatarUrl = extractAvatarUrl(registrationId, attributes);
        String providerId = extractProviderId(registrationId, attributes);
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        // Si el email es nulo, el login no puede continuar.
        if (email == null) {
            log.error("El email recibido del proveedor {} es nulo. Revisa los 'scopes' de tu configuración.", registrationId);
            throw new OAuth2AuthenticationException("No se pudo obtener el email del proveedor de OAuth2.");
        }
        
        log.info("Datos extraídos -> Email: [{}], Nombre: [{}], Avatar: [{}]", email, name, avatarUrl);

        // Buscar o crear usuario
        User user = userService.findOrCreateOAuth2User(
            email, 
            name, 
            avatarUrl, 
            provider, 
            providerId
        );
        
        log.info("Usuario procesado en la DB: {} ({})", user.getEmail(), user.getProvider());
        
        return new CustomOAuth2User(oauth2User, user);
    }
    
    // --- MÉTODOS DE EXTRACCIÓN MÁS ROBUSTOS ---

    private String extractEmail(String provider, Map<String, Object> attributes) {
        // GitHub puede no devolver el email si no se pide el scope 'user:email'
        // o si el usuario no lo tiene público.
        return (String) attributes.get("email");
    }
    
    private String extractName(String provider, Map<String, Object> attributes) {
        String extractedName = (String) attributes.get("name");
        
        // Si el nombre es nulo (común en GitHub), usamos un fallback.
        if (extractedName == null || extractedName.isBlank()) {
            if ("github".equalsIgnoreCase(provider)) {
                // El 'login' de GitHub siempre existe.
                return (String) attributes.get("login");
            }
            if ("discord".equalsIgnoreCase(provider)) {
                String globalName = (String) attributes.get("global_name");
                return globalName != null ? globalName : (String) attributes.get("username");
            }
            // Fallback final: coge el email y quítale el @...
            String email = (String) attributes.get("email");
            if (email != null) {
                return email.split("@")[0];
            }
        }
        return extractedName;
    }
    
    private String extractProviderId(String provider, Map<String, Object> attributes) {
        // El 'id' en GitHub es un número, hay que pasarlo a String.
        Object id = attributes.get("id");
        if (id != null && "github".equalsIgnoreCase(provider)) {
            return id.toString();
        }
        return (String) attributes.get("sub"); // 'sub' es el estándar para Google
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
                yield null; // Sin fallback si no hay avatar
            }
            default -> null;
        };
    }
}