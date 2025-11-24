package com.gamescore.back.controller;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.User;
import com.gamescore.back.model.Review;
import com.gamescore.back.model.enums.ReviewStatus;
import com.gamescore.back.service.DashboardService;
import com.gamescore.back.service.GameService;
import com.gamescore.back.service.UserService; // Asegúrate de que este servicio tenga los métodos findAll() o findByRole()
import com.gamescore.back.service.ReviewService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final GameService gameService;
    private final DashboardService dashboardService;
    private final ReviewService reviewService;
    // 1. Inyección de UserService (Necesario para que funcione)
    private final UserService userService;

    // =======================================================================
    // VISTAS DEL DASHBOARD (GENERAL Y JUEGOS)
    // =======================================================================

    @GetMapping
    public String showDashboardAsIndex(Model model) {
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
    public String manageUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            Model model) {

        List<User> users;

        // Caso: se filtra por rol y keyword
        if ((role != null && !role.isEmpty()) && (keyword != null && !keyword.isEmpty())) {
            users = userService.findByRoleAndKeyword(role, keyword);
        }
        // Caso: solo rol
        else if (role != null && !role.isEmpty()) {
            users = userService.findAllFilteredByRole(role);
        }
        // Caso: solo keyword
        else if (keyword != null && !keyword.isEmpty()) {
            users = userService.findByKeyword(keyword);
        }
        // Caso: ningún filtro
        else {
            users = userService.findAll();
        }

        model.addAttribute("users", users);
        model.addAttribute("selectedRole", role); // Mantener rol seleccionado
        model.addAttribute("keyword", keyword); // Mantener keyword en el input
        return "admin/users";
    }

    @PostMapping("usuarios/eliminar")
    public String eliminarUsuario(@RequestParam("id") Long id) {
        try {
            userService.delete(id); // Llama a tu servicio para borrar
        } catch (Exception e) {
            // Manejo de errores opcional
            e.printStackTrace();
        }
        // Redirige de nuevo a la lista de usuarios para ver los cambios
        return "redirect:/admin/usuarios";
    }

    // =======================================================================
    // GESTIÓN DE RESEÑAS (MODERACIÓN)
    // =======================================================================

    @GetMapping("/resenas")
    public String manageReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) Integer rating,
            Model model) {

        List<Review> reviews;

        if (keyword != null && !keyword.isEmpty()) {
            reviews = reviewService.searchWithFilters(keyword, status, rating);
        } else if (status != null || rating != null) {
            reviews = reviewService.searchWithFilters(null, status, rating);
        } else {
            reviews = reviewService.findAllReviews();
        }

        long pendingCount = reviewService.countPendingReviews();

        model.addAttribute("reviews", reviews);
        model.addAttribute("keyword", keyword);
        model.addAttribute("paramStatus", status);
        model.addAttribute("paramRating", rating);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("statuses", ReviewStatus.values());

        return "admin/reviews";
    }

    @PostMapping("/resenas/moderar/{id}")
    public String moderateReview(
            @PathVariable Long id,
            @RequestParam ReviewStatus status,
            @RequestParam(required = false) String reviewNote,
            RedirectAttributes redirectAttributes) {

        Optional<Review> optionalReview = reviewService.findReviewById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setStatus(status);
            review.setReviewNote(reviewNote);
            if (status == ReviewStatus.APPROVED) {
                review.setApprovedAt(LocalDateTime.now());
            }
            reviewService.save(review);
            redirectAttributes.addFlashAttribute("successMessage", "Reseña moderada correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Reseña no encontrada.");
        }

        return "redirect:/admin/resenas";
    }

    // =======================================================================
    // GESTIÓN DE JUEGOS (CREATE/UPDATE)
    // =======================================================================

    @GetMapping("/juegos/nuevo")
    public String newGameForm(Model model) {
        model.addAttribute("game", new Game());
        return "admin/game-form";
    }

    @GetMapping("/juegos/editar/{id}")
    public String editGameForm(@PathVariable Long id, Model model) {
        Game game = gameService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de juego inválido: " + id));

        model.addAttribute("game", game);
        return "admin/game-form";
    }

    @PostMapping("/juegos/editar/{id}")
    public String updateGame(@PathVariable Long id, @ModelAttribute("game") Game gameForm) {
        Game game = gameService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado"));

        game.setName(gameForm.getName());
        game.setSlug(gameForm.getSlug());
        game.setRawgId(gameForm.getRawgId());
        game.setReleaseDate(gameForm.getReleaseDate());
        game.setDescription(gameForm.getDescription());
        game.setBackgroundUrl(gameForm.getBackgroundUrl());
        game.setWebsite(gameForm.getWebsite());
        game.setYoutubeUrl(gameForm.getYoutubeUrl());
        game.setRating(gameForm.getRating());
        game.setMetacritic(gameForm.getMetacritic());

        gameService.save(game);
        return "redirect:/admin/juegos";
    }
}