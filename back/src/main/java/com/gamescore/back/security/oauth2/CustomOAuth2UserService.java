package com.gamescore.back.security.oauth2;

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
        
        log.info("OAuth2 Login - Provider: {}", registrationId);
        
        // Extraer información según el proveedor
        String email = extractEmail(registrationId, attributes);
        String name = extractName(registrationId, attributes);
        String avatarUrl = extractAvatarUrl(registrationId, attributes);
        String providerId = extractProviderId(registrationId, attributes);
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        
        // Buscar o crear usuario
        User user = userService.findOrCreateOAuth2User(
            email, 
            name, 
            avatarUrl, 
            provider, 
            providerId
        );
        
        log.info("Usuario OAuth2 procesado: {} ({})", email, provider);
        
        return new CustomOAuth2User(oauth2User, user);
    }
    
    private String extractEmail(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("email");
            case "github" -> (String) attributes.get("email");
            case "discord" -> (String) attributes.get("email");
            default -> throw new OAuth2AuthenticationException("Proveedor no soportado: " + provider);
        };
    }
    
    private String extractName(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("name");
            case "github" -> (String) attributes.get("name");
            case "discord" -> {
                String username = (String) attributes.get("username");
                String globalName = (String) attributes.get("global_name");
                yield globalName != null ? globalName : username;
            }
            default -> "Usuario";
        };
    }
    
    private String extractProviderId(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("sub");
            case "github" -> attributes.get("id").toString();
            case "discord" -> (String) attributes.get("id");
            default -> null;
        };
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