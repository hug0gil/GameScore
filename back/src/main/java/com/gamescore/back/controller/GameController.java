package com.gamescore.back.controller;

import com.gamescore.back.model.Game;
import com.gamescore.back.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/")
    public String showIndexPage() {
        return "index";
    }

    @GetMapping("/juegos")
    public String showGamesListPage(Model model) {
        model.addAttribute("games", gameService.findAll());
        model.addAttribute("featuredGames", gameService.findFeatured());
        return "games";
    }

    @GetMapping("/juego/{id}")
    public String showGameDetailPage(@PathVariable("id") Long id, Model model) {
        Game game = gameService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado"));
        model.addAttribute("game", game);
        return "game-detail";
    }
}