package com.gamescore.back.controller;

import com.gamescore.back.security.CustomOAuth2User; // <-- IMPORTANTE: Importa tu clase
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    /**
     * Muestra la página de perfil del usuario autenticado.
     * 
     * @param customOAuth2User Inyectado por Spring. Este es TU objeto personalizado
     *                         que contiene la entidad 'User'.
     * @param model            El modelo para pasar datos a la vista.
     * @return El nombre de la plantilla de perfil.
     */
    @GetMapping("/perfil")
    public String profile(Model model, @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        // 1. Verificamos que el principal (el usuario logueado) no sea nulo.
        if (customOAuth2User == null) {
            // Si no está autenticado, lo mandamos al login.
            return "/login";
        }

        // 2. Obtenemos el objeto 'User' que está dentro de tu 'CustomOAuth2User'.
        // Tu clase CustomOAuth2User tiene un campo 'user' y un getter gracias a Lombok
        // (@Getter).
        model.addAttribute("user", customOAuth2User.getUser());

        // 3. Devolvemos el nombre de la vista.
        return "perfil";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Nombre de la plantilla HTML
    }
}