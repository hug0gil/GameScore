package com.gamescore.back.controller;

import com.gamescore.back.model.User;
import com.gamescore.back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    // --- LOGIN (GET) ---
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "logout", required = false) String logout, Model model) {
        if (logout != null) {
            model.addAttribute("logoutMsg", "Has cerrado sesión correctamente.");
        }
        return "login";
    }

    // --- REGISTER (GET) -> ESTE ES EL QUE TE FALTA O FALLA ---
    @GetMapping("/registro")
    public String registerPage(Model model) {
        // Importante: Pasamos un objeto vacío para que Thymeleaf pueda enlazar los campos
        model.addAttribute("user", new User());
        return "register";
    }

    // --- REGISTER (POST) -> ESTE PROCESA EL FORMULARIO ---
    @PostMapping("/registro")
    public String registerUser(@ModelAttribute("user") User user, 
                               BindingResult result, 
                               Model model) {
        
        // 1. Validar errores de formulario
        if (result.hasErrors()) {
            return "register";
        }

        try {
            // 2. Registrar usuario
            userService.registerUser(user);
            
            // 3. Redirigir al login si todo sale bien
            return "redirect:/login?registered"; 
            
        } catch (IllegalArgumentException e) {
            // 4. Capturar errores de negocio (email duplicado, etc.)
            model.addAttribute("errorMessage", e.getMessage());
            return "register"; // Volver al formulario con el error
        }
    }
}