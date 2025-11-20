package com.gamescore.back.controller;

import com.gamescore.back.security.CustomOAuth2User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class ProfileController {

    @GetMapping("/perfil")
    public String profile(Model model, @AuthenticationPrincipal(errorOnInvalidType = false) Object principal) {
        
        log.info("--- INICIANDO DEPURACIÓN DE /perfil ---");

        if (principal == null) {
            log.error("¡ERROR GRAVE! El 'principal' (usuario autenticado) es NULO. Spring Security no lo está inyectando.");
            // Forzamos un error más claro en lugar de un NullPointerException
            throw new IllegalStateException("El principal de seguridad es nulo. Verifique la configuración de autenticación.");
        }

        log.info("Clase del principal recibido: {}", principal.getClass().getName());
        
        if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;
            log.info("El principal es de tipo 'CustomOAuth2User'. ¡Correcto!");
            log.info("Email del usuario: {}", customOAuth2User.getEmail());
            
            model.addAttribute("user", customOAuth2User.getUser());
            log.info("--- DEPURACIÓN COMPLETADA --- Renderizando vista 'perfil'.");
            return "profile";
        } else {
            log.error("¡ERROR DE TIPO! El principal NO es 'CustomOAuth2User', sino '{}'.", principal.getClass().getName());
            log.error("Contenido del principal: {}", principal.toString());
            throw new IllegalStateException("El tipo del principal es incorrecto. Se esperaba CustomOAuth2User.");
        }
    }
    
}