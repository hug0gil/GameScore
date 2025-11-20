package com.gamescore.back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    /**
     * Muestra la página de inicio de sesión con los botones de OAuth2.
     * Esta ruta es a la que Spring Security redirige a los usuarios no autenticados.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Devuelve el nombre del archivo HTML (login.html)
    }
}