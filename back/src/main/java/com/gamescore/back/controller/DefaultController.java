package com.gamescore.back.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DefaultController {

    @GetMapping("/")
    public String showIndexPage(Model model) {
        return "index";
    }

    @GetMapping("/sobre-nosotros")
    public String sobreNosotros() {
        return "sobre-nosotros"; // nombre del template HTML en templates/
    }

    @GetMapping("/politica-privacidad")
    public String politicaPrivacidad() {
        return "politica-privacidad"; // nombre del template HTML en templates/
    }

}