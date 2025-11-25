package com.gamescore.back.controller;

import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.repository.UserRepository;
import com.gamescore.back.service.EmailService;
import com.gamescore.back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;
    private final EmailService emailService;
    private final UserRepository userRepository; // Necesario para buscar por token
    private final PasswordEncoder passwordEncoder; // Necesario para encriptar la nueva clave

    // 1. Mostrar formulario de "Ingresa tu email"
    @GetMapping("/olvide-password")
    public String showForgotPasswordForm() {
        return "forgot_password"; // Asegúrate de que existe templates/forgot_password.html
    }

    // 2. Procesar el email y enviar correo
    @PostMapping("/olvide-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            // A. Buscar usuario (usamos userService que ya tiene findByEmail)
            User user = userService.findByEmail(email);

            // B. Validar que sea LOCAL (Google/Discord no tienen contraseña)
            if (user.getProvider() != AuthProvider.LOCAL) {
                throw new IllegalArgumentException("Esta cuenta está vinculada a " + user.getProvider() + ". Inicia sesión con ese proveedor.");
            }

            // C. Generar Token y Expiración
            String token = UUID.randomUUID().toString();
            user.setResetPasswordToken(token);
            user.setTokenExpirationDate(LocalDateTime.now().plusMinutes(30)); // 30 min validez

            // D. Guardar el token en BD
            userService.save(user);

            // E. ENVIAR EMAIL (Llamada directa al servicio)
            emailService.sendPasswordReset(user.getEmail(), token);

            model.addAttribute("message", "Hemos enviado un enlace de recuperación a tu correo.");

        } catch (Exception e) {
            // Si el usuario no existe, por seguridad a veces se recomienda no decir nada,
            // pero para desarrollo mostramos el error:
            model.addAttribute("error", e.getMessage());
        }
        return "forgot_password";
    }

    // 3. Mostrar formulario de "Nueva contraseña" (Viene del click en el email)
    @GetMapping("/cambiar-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // Validar token manualmente aquí porque lo quitamos del servicio
        User user = userRepository.findByResetPasswordToken(token)
                .orElse(null);

        if (user == null || user.getTokenExpirationDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "El enlace es inválido o ha expirado.");
            return "forgot_password";
        }

        model.addAttribute("token", token); // Pasamos el token a la vista para el POST
        return "reset_password"; // Asegúrate de que existe templates/reset_password.html
    }

    // 4. Guardar la nueva contraseña
    @PostMapping("/cambiar-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       Model model) {
        try {
            // Buscar usuario por token
            User user = userRepository.findByResetPasswordToken(token)
                    .orElseThrow(() -> new IllegalArgumentException("Token inválido."));

            // Validar expiración de nuevo por seguridad
            if (user.getTokenExpirationDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("El enlace ha expirado.");
            }

            // Actualizar contraseña
            user.setPassword(passwordEncoder.encode(password));
            
            // Limpiar token
            user.setResetPasswordToken(null);
            user.setTokenExpirationDate(null);

            userService.save(user);

            // Redirigimos al login con mensaje de éxito
            return "redirect:/login?resetSuccess";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "reset_password";
        }
    }
}