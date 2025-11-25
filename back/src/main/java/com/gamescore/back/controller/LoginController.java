package com.gamescore.back.controller;

import com.gamescore.back.model.User;
import com.gamescore.back.service.EmailService;
import com.gamescore.back.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final UserService userService;
    private final EmailService emailService;

    // --- LOGIN (GET) ---
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "logout", required = false) String logout, Model model) {
        if (logout != null) {
            model.addAttribute("logoutMsg", "Has cerrado sesión correctamente.");
        }
        return "login";
    }

    // --- REGISTER (GET) ---
    @GetMapping("/registro") // URL: http://localhost:8080/registro
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Archivo: templates/register.html
    }

    // --- REGISTER (POST) ---
    @PostMapping("/registro")
    public String registerUser(@ModelAttribute("user") User user, 
                               BindingResult result, 
                               Model model) {
        
        if (result.hasErrors()) {
            return "register"; // Si hay error, volvemos a templates/registro.html
        }

        try {
            // 1. Registrar usuario
            User savedUser = userService.registerUser(user);
            
            // 2. Enviar Email
            try {
                log.info("Enviando email de confirmación a: {}", savedUser.getEmail());
                emailService.sendWelcomeAndConfirmation(savedUser.getEmail(), savedUser.getName());
            } catch (Exception e) {
                log.error("Error al enviar email de bienvenida", e);
            }
            
            return "redirect:/login?registered"; 
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }
}