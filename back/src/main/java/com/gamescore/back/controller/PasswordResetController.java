package com.gamescore.back.controller;

import com.gamescore.back.model.User;
import com.gamescore.back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;

    // 1. Mostrar formulario de "Ingresa tu email"
    @GetMapping("/olvide-password")
    public String showForgotPasswordForm() {
        return "forgot_password";
    }

    // 2. Procesar el email y enviar correo
    @PostMapping("/olvide-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            userService.requestPasswordReset(email);
            model.addAttribute("message", "Hemos enviado un enlace de recuperación a tu correo.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forgot_password";
    }

    // 3. Mostrar formulario de "Nueva contraseña" (Viene del click en el email)
    @GetMapping("/cambiar-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        try {
            userService.getByResetToken(token); // Validamos que exista y no haya expirado
            model.addAttribute("token", token); // Pasamos el token oculto a la vista
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "forgot_password"; // Si está mal, lo mandamos de vuelta a pedir otro
        }
        return "reset_password";
    }

    // 4. Guardar la nueva contraseña
    @PostMapping("/cambiar-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       Model model) {
        try {
            User user = userService.getByResetToken(token);
            userService.updatePassword(user, password);
            // Redirigimos al login con mensaje de éxito
            return "redirect:/login?resetSuccess"; 
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "reset_password";
        }
    }
}