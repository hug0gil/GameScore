package com.gamescore.back.controller;

import com.gamescore.back.model.User;
import com.gamescore.back.service.EmailService;
import com.mailjet.client.errors.MailjetException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private EmailService emailService;

    /**
     * Obtiene la información del usuario actual
     * Angular llama esto después de recibir el JWT
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal User user) {
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
     * 
     * @return El nombre de la plantilla "login".
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) throws MailjetException {
        // No validamos si existe o no por seguridad, o enviamos mensaje genérico
        String html = "<h3>Solicitud de Contraseña</h3>"
                + "<p>Recibimos una solicitud para recuperar tu cuenta.</p>"
                + "<p>En <strong>GameScore</strong> inicias sesión con Google/Discord/Github.</p>"
                + "<p>Por favor, usa esos botones en la pantalla de inicio.</p>";

        // Enviamos siempre (o validas si existe en DB antes)
        emailService.sendEmail(email, "Usuario", "Acceso a tu cuenta GameScore", html);

        return ResponseEntity.ok("Si el correo existe, recibirás instrucciones.");
    }
}