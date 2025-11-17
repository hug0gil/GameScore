package com.gamescore.back.controller;

import com.gamescore.back.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    /**
     * Obtiene la información del usuario actual
     * Angular llama esto después de recibir el JWT
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.debug("Obteniendo información del usuario: {}", user.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("avatarUrl", user.getAvatarUrl());
        response.put("role", user.getRole().name());
        response.put("provider", user.getProvider().name());
        response.put("enabled", user.getEnabled());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Muestra la página de login, que ahora contiene los botones de OAuth2.
     * @return El nombre de la plantilla "login".
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}