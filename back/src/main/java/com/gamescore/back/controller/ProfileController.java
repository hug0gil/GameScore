package com.gamescore.back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.gamescore.back.model.User;

import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

    @Controller
public class ProfileController {

    @GetMapping("/perfil")
    public String profile(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/perfil/editar")
    public String editProfileForm(Model model, @AuthenticationPrincipal User user) {
        // Lógica si haces editar perfil
        return "profile-edit-page";
    }
}
