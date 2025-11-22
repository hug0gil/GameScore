package com.gamescore.back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    /**
     * Muestra la página de inicio de sesión con los botones de OAuth2.
     * Esta ruta es a la que Spring Security redirige a los usuarios no
     * autenticados.
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (logout != null) {
            model.addAttribute("logoutMsg", "Has cerrado sesión correctamente.");
        }
        return "login"; // Devuelve login.html
    }

}