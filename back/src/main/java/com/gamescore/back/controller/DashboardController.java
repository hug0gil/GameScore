package com.gamescore.back.controller;

import com.gamescore.back.service.GameService;
import com.gamescore.back.service.UserService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final GameService gameService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // model.addAttribute("stats", statsService.getDashboardStats());
        // model.addAttribute("chartData", chartService.getUserChartData());
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
        // model.addAttribute("game", new GameDto());
        return "admin/game-form";
    }

    @GetMapping("/juegos/editar/{id}")
    public String editGame(@PathVariable Long id, Model model) {
        // model.addAttribute("game", gameService.findDtoById(id));
        return "admin/game-form";
    }
}
