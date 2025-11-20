package com.gamescore.back.controller;

import com.gamescore.back.model.Game;
import com.gamescore.back.service.DashboardService;
import com.gamescore.back.service.GameService;
import com.gamescore.back.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
// @PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final GameService gameService;
    private final UserService userService;
    private final DashboardService dashboardService;

    @GetMapping
    public String showDashboardAsIndex(Model model) {
        // Tienes que duplicar la lógica de tu método de dashboard
        model.addAttribute("stats", dashboardService.getDashboardStats());
        model.addAttribute("chartData", dashboardService.getNewUserChartData());
        return "admin/dashboard";
    }

    @GetMapping("/juegos")
    public String manageGames(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("games", gameService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "admin/games";
    }

    @GetMapping("/usuarios")
    public String manageUsers(@RequestParam(required = false) String role, Model model) {
        // model.addAttribute("users", userService.findAllFilteredByRole(role));
        return "admin/users";
    }

    @GetMapping("/resenas")
    public String manageReviews(@RequestParam(required = false) String keyword, Model model) {
        // model.addAttribute("reviews", reviewService.search(keyword));
        return "admin/reviews";
    }

    @GetMapping("/juegos/nuevo")
    public String newGameForm(Model model) {
        // Pasamos un objeto Game nuevo y vacío a la vista
        model.addAttribute("game", new Game());
        return "admin/game-form";
    }

    @GetMapping("/juegos/editar/{id}")
    public String editGameForm(@PathVariable Long id, Model model) {
        // Buscamos el juego por ID. Si no existe, lanzamos una excepción
        // (o podrías redirigir a una página de error).
        Game game = gameService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de juego inválido: " + id));

        // Pasamos el juego encontrado al modelo.
        model.addAttribute("game", game);
        return "admin/game-form";
    }
}