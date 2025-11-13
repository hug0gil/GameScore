package com.gamescore.back.controller;

import com.gamescore.back.model.Game;
import com.gamescore.back.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/games")
public class GameViewController {

    @Autowired
    private GameService gameService;

    @GetMapping
    public String mostrarJuegos(Model model) {
        List<Game> juegos = gameService.findAll();
        model.addAttribute("listaJuegos", juegos);
        return "games/list"; // Ir a templates/games/list.html
    }
}