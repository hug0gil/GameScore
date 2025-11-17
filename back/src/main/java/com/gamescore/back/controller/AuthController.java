package com.gamescore.back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    /**
     * Muestra la página de login, que ahora contiene los botones de OAuth2.
     * @return El nombre de la plantilla "login".
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    /**
     * Muestra la página de registro.
     * @return El nombre de la plantilla "register".
     */
    @GetMapping("/registro")
    public String showRegisterPage() {
        return "register";
    }
}
