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

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
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
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final GameService gameService;
    private final DashboardService dashboardService;
    private final ReviewService reviewService;
    private final UserService userService;

    public void addCsrfToken(Model model, CsrfToken token) {
        model.addAttribute("_csrf", token);
    }

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
    public String listGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            Model model) {

        int pageSize = 10; // 10 juegos por página
        Page<Game> gamesPage;

        if (keyword != null && !keyword.isEmpty()) {
            gamesPage = gameService.searchPagedByName(keyword, page, pageSize);
        } else {
            gamesPage = gameService.findAllPaged(page, pageSize);
        }

        model.addAttribute("games", gamesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", gamesPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/games"; // tu vista Thymeleaf
    }

    @GetMapping("/usuarios")
    public String manageUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int pageSize = 5;
        Page<User> usersPage;

        if ((role != null && !role.isEmpty()) && (keyword != null && !keyword.isEmpty())) {
            usersPage = userService.searchPagedByRoleAndKeyword(role, keyword, page, pageSize);
        } else if (role != null && !role.isEmpty()) {
            usersPage = userService.findAllPagedByRole(role, page, pageSize);
        } else if (keyword != null && !keyword.isEmpty()) {
            usersPage = userService.searchPagedByKeyword(keyword, page, pageSize);
        } else {
            usersPage = userService.findAllPaged(page, pageSize);
        }

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("selectedRole", role);
        model.addAttribute("keyword", keyword);

        return "admin/users";
    }

    @PostMapping("usuarios/eliminar/{id}")
    public String deleteUser(@PathVariable Long id) {
        try {
            userService.delete(id); // Llama a tu servicio para borrar
        } catch (Exception e) {
            // Manejo de errores opcional
            e.printStackTrace();
        }
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
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int pageSize = 3;
        Page<Review> reviewsPage;

        if (keyword != null && !keyword.isEmpty()) {
            reviewsPage = reviewService.searchPagedWithFilters(keyword, status, rating, page, pageSize);
        } else if (status != null || rating != null) {
            reviewsPage = reviewService.searchPagedWithFilters(null, status, rating, page, pageSize);
        } else {
            reviewsPage = reviewService.findAllPaged(page, pageSize);
        }

        long pendingCount = reviewService.countPendingReviews();

        model.addAttribute("reviews", reviewsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewsPage.getTotalPages());
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

    @PostMapping("/juegos/crear")
    public String crearJuego(@ModelAttribute("game") Game game) {
        gameService.save(game);
        return "redirect:/admin/juegos";
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