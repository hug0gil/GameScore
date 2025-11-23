package com.gamescore.back.controller;

import com.gamescore.back.model.Game;
import com.gamescore.back.model.User; // ⬅️ IMPORTANTE: Necesario para obtener el ID del moderador
import com.gamescore.back.model.Review; // ⬅️ Nuevo: Para el formulario de moderación
import com.gamescore.back.model.enums.ReviewStatus; // ⬅️ Nuevo: Para el estado de moderación
import com.gamescore.back.service.DashboardService;
import com.gamescore.back.service.GameService;
import com.gamescore.back.service.UserService;
import com.gamescore.back.service.ReviewService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Nuevo: Para acceder a los detalles del usuario
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

import java.security.Principal; // Nuevo: Para el usuario autenticado
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final GameService gameService;
    private final UserService userService;
    private final DashboardService dashboardService;
    private final ReviewService reviewService; // Inyección de ReviewService

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
    public String manageUsers(@RequestParam(required = false) String role, Model model) {
        // model.addAttribute("users", userService.findAllFilteredByRole(role));
        return "admin/users";
    }

    // =======================================================================
    // GESTIÓN DE RESEÑAS (MODERACIÓN)
    // =======================================================================

    @GetMapping("/resenas")
    public String manageReviews(@RequestParam(required = false) String keyword, Model model) {
        // Usa el método search corregido en ReviewService que trae todas las reseñas (PENDING/APPROVED/REJECTED)
        model.addAttribute("reviews", reviewService.search(keyword)); 
        model.addAttribute("keyword", keyword); 
        return "admin/reviews";
    }

    /**
     * Muestra el formulario para moderar una reseña específica.
     * Asume la existencia de la plantilla 'admin/review-moderate-form.html'.
     */
    @GetMapping("/resenas/moderar/{id}")
    public String showModerateReviewForm(@PathVariable Long id, Model model) {
        Review review = reviewService.findReviewById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reseña no encontrada: " + id));

        model.addAttribute("review", review);
        return "admin/review-moderate-form";
    }

    /**
     * Procesa la moderación de la reseña (APROBAR/RECHAZAR).
     */
    @PostMapping("/resenas/moderar/{id}")
    public String moderateReview(
            @PathVariable Long id, 
            @RequestParam ReviewStatus status, 
            @RequestParam(required = false) String reviewNote,
            Principal principal, // Obtiene el objeto Principal del Admin autenticado
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Obtener el ID del moderador
            User moderator = getModeratorUser(principal); 
            Long moderatorId = moderator.getId(); // Usamos el ID del User completo

            // 2. Ejecutar el servicio de moderación
            reviewService.moderateReview(id, status, moderatorId, reviewNote);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Reseña ID " + id + " ha sido marcada como " + status.name() + ".");

        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Reseña o moderador no encontrado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al moderar la reseña: " + e.getMessage());
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

        // Actualizamos los campos
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

    // =======================================================================
    // MÉTODOS AUXILIARES
    // =======================================================================

    /**
     * Obtiene el objeto User completo del moderador autenticado.
     * ⚠️ NOTA IMPORTANTE: Debes asegurarte de que este método coincide con la lógica
     * de autenticación de tu UserService/seguridad.
     */
    private User getModeratorUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay administrador autenticado.");
        }
        
        // Asumiendo que principal.getName() devuelve el email o username único
        String emailOrUsername = principal.getName();
        
        // ⚠️ REEMPLAZAR ESTA LÍNEA por tu lógica para buscar el User por email/username
        // Esto asume que tienes un método en UserService que busca por ese campo.
        return userService.findByEmailOrUsername(emailOrUsername) 
               .orElseThrow(() -> new NoSuchElementException("Usuario administrador no encontrado en BD."));
    }
}