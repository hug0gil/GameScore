package com.gamescore.back.service;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.User;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private MailjetClient mailjetClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${mailjet.sender.email}")
    private String senderEmail;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ============================================================
    // 1. MÉTODOS DE NEGOCIO
    // ============================================================

    /**
     * Envía el resumen semanal
     */
    public void sendWeeklySummary(User user) {
        Context context = new Context();
        
        context.setVariable("user", user);
        context.setVariable("loginCount", user.getLoginCount());
        
        int favCount = (user.getFavorites() != null) ? user.getFavorites().size() : 0;
        context.setVariable("favoritesCount", favCount);
        
        List<String> favNames = new ArrayList<>();
        if (user.getFavorites() != null && !user.getFavorites().isEmpty()) {
            favNames = user.getFavorites().stream()
                    .limit(3)
                    .map(Game::getName)
                    .collect(Collectors.toList());
        } else {
            favNames.add("Ninguno aún");
        }
        context.setVariable("favoriteGamesList", favNames);

        String htmlContent = templateEngine.process("email/summary-template", context);
        sendEmail(user.getEmail(), user.getName(), "🎮 Tus Estadísticas de GameScore", htmlContent);
    }

    /**
     * Envía bienvenida
     * NOTA: Como quitamos el token, asegúrate de que LoginController
     * llame a este método pasando solo (email, name).
     */
    public void sendWelcomeAndConfirmation(String email, String name) {
        Context context = new Context();
        context.setVariable("username", name);
        
        // Redirige al Login directamente
        context.setVariable("confirmationUrl", baseUrl + "/login");

        String htmlContent = templateEngine.process("email/welcome-template", context);

        sendEmail(email, name, "¡Bienvenido a GameScore!", htmlContent);
    }

    /**
     * Envía link de recuperación de contraseña
     */
    public void sendPasswordReset(String email, String token) {
        Context context = new Context();
        
        // --- CORRECCIÓN AQUÍ ---
        // Debe coincidir con @GetMapping("/cambiar-password") del PasswordResetController
        context.setVariable("resetUrl", baseUrl + "/cambiar-password?token=" + token);

        String htmlContent = templateEngine.process("email/reset-password-template", context);

        sendEmail(email, "Usuario", "Recuperar Contraseña - GameScore", htmlContent);
    }

    // ============================================================
    // 2. MÉTODO GENÉRICO
    // ============================================================

    public void sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        try {
            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                    .put(new JSONObject()
                        .put(Emailv31.Message.FROM, new JSONObject()
                            .put("Email", senderEmail)
                            .put("Name", "GameScore"))
                        .put(Emailv31.Message.TO, new JSONArray()
                            .put(new JSONObject()
                                .put("Email", toEmail)
                                .put("Name", toName != null ? toName : "Usuario")))
                        .put(Emailv31.Message.SUBJECT, subject)
                        .put(Emailv31.Message.HTMLPART, htmlContent)
                        .put(Emailv31.Message.CUSTOMID, "GameScoreApp")));

            MailjetResponse response = mailjetClient.post(request);

            if (response.getStatus() != 200) {
                System.err.println("Error Mailjet: " + response.getStatus() + " " + response.getData());
            }
        } catch (MailjetException e) {
            e.printStackTrace();
        }
    }
}