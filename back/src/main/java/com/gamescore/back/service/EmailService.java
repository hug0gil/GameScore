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
    private TemplateEngine templateEngine; // Necesario para procesar los HTML

    @Value("${mailjet.sender.email}")
    private String senderEmail;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ============================================================
    // 1. MÉTODOS DE NEGOCIO (Los que llaman tus Controladores)
    // ============================================================

    /**
     * Envía el resumen semanal (Llamado desde ProfileController)
     */
public void sendWeeklySummary(User user) {
    Context context = new Context();
    
    // Pasamos el usuario
    context.setVariable("user", user);
    
    // DATO REAL 1: Contador de Logins
    context.setVariable("loginCount", user.getLoginCount());
    
    // DATO REAL 2: Cantidad de Favoritos (Protegemos contra nulos)
    int favCount = (user.getFavorites() != null) ? user.getFavorites().size() : 0;
    context.setVariable("favoritesCount", favCount);
    
    // DATO REAL 3: Lista de nombres de los 3 primeros favoritos
    List<String> favNames = new ArrayList<>();
    if (user.getFavorites() != null && !user.getFavorites().isEmpty()) {
        favNames = user.getFavorites().stream()
                .limit(3)
                .map(Game::getName) // Asumiendo que Game tiene getName()
                .collect(Collectors.toList());
    } else {
        favNames.add("Ninguno aún");
    }
    context.setVariable("favoriteGamesList", favNames);

    // Procesar plantilla
    String htmlContent = templateEngine.process("email/summary-template", context);
    sendEmail(user.getEmail(), user.getName(), "🎮 Tu Estadísticas de GameScore", htmlContent);
}

    /**
     * Envía bienvenida y link de confirmación (Llamado desde AuthController)
     */
    public void sendWelcomeAndConfirmation(String email, String name, String token) {
        Context context = new Context();
        context.setVariable("username", name);
        context.setVariable("confirmationUrl", baseUrl + "/auth/confirm?token=" + token);

        // Asegúrate de tener 'email/welcome-template.html' creado
        // Si no lo tienes, usa una cadena simple por ahora o crea el archivo
        String htmlContent = templateEngine.process("email/welcome-template", context);

        sendEmail(email, name, "¡Bienvenido a GameScore! Confirma tu cuenta", htmlContent);
    }

    /**
     * Envía link de recuperación de contraseña (Llamado desde AuthController)
     */
    public void sendPasswordReset(String email, String token) {
        Context context = new Context();
        context.setVariable("resetUrl", baseUrl + "/auth/reset-password?token=" + token);

        // Asegúrate de tener 'email/reset-password-template.html'
        String htmlContent = templateEngine.process("email/reset-password-template", context);

        sendEmail(email, "Usuario", "Recuperar Contraseña - GameScore", htmlContent);
    }

    // ============================================================
    // 2. MÉTODO PUBLIC GENÉRICO (Conexión con Mailjet)
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
                // Loguear error pero no romper la app
                System.err.println("Error Mailjet: " + response.getStatus() + " " + response.getData());
            }
        } catch (MailjetException e) {
            e.printStackTrace();
            // Opcional: Lanzar RuntimeException si quieres que el controlador se entere
        }
    }
}