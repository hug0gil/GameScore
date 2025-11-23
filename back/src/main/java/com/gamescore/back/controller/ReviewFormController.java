package com.gamescore.back.controller;

import com.gamescore.back.model.Review;
import com.gamescore.back.model.Game;
import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.ReviewStatus;
import com.gamescore.back.service.GameService;
import com.gamescore.back.service.ReviewService;
import com.gamescore.back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewFormController {

    private final GameService gameService;
    private final ReviewService reviewService;
    private final UserService userService;

    /**
     * Muestra el formulario para crear una nueva reseña.
     */
    @GetMapping("/nueva")
    public String showNewReviewForm(@RequestParam Long gameId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Game game = gameService.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Juego no encontrado con ID: " + gameId));

        Review newReview = new Review();
        newReview.setGame(game);

        model.addAttribute("review", newReview);
        // CORREGIDO: Usamos "currentGame" para coincidir con el HTML
        model.addAttribute("currentGame", game);

        return "review-form";
    }

    /**
     * Procesa el envío del formulario para crear una nueva reseña.
     */
    @PostMapping("/nueva")
    public String createReview(@ModelAttribute("review") Review review,
                               BindingResult result,
                               Model model, // Necesitamos Model aquí para errores, no solo RedirectAttributes
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        // Recuperamos el juego usando el ID que viene en el formulario oculto
        // Esto es necesario tanto para errores como para el slug de redirección
        Long gameId = (review.getGame() != null) ? review.getGame().getId() : null;
        Game game = null;
        
        if (gameId != null) {
            game = gameService.findById(gameId).orElse(null);
        }

        // 1. Manejo de ERRORES DE VALIDACIÓN
        if (result.hasErrors() || game == null) {
            if (game == null) {
                return "redirect:/"; // Si no hay ID de juego, error fatal, ir a home
            }
            
            // CORREGIDO: Usamos Model, no RedirectAttributes, porque retornamos la vista directamente
            model.addAttribute("currentGame", game);
            // 'review' y 'org.springframework.validation.BindingResult.review' 
            // se agregan automáticamente al modelo al retornar la vista si usamos @ModelAttribute
            return "review-form";
        }

        try {
            User loggedInUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new NoSuchElementException("Usuario logueado no encontrado."));

            review.setUser(loggedInUser);
            review.setGame(game); // Asignamos el objeto game completo cargado arriba

            reviewService.createReview(review);

            redirectAttributes.addFlashAttribute("successMessage", "¡Reseña creada y enviada para moderación!");
            return "redirect:/juego/" + game.getSlug();

        } catch (Exception e) {
            // Error lógico (ej. reseña duplicada)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            
            // Si falla, intentamos redirigir al juego usando el objeto game cargado previamente
            if (game != null && game.getSlug() != null) {
                return "redirect:/juego/" + game.getSlug();
            }
            return "redirect:/";
        }
    }

    /**
     * Muestra el formulario para editar una reseña existente.
     */
    @GetMapping("/editar/{id}")
    public String showEditReviewForm(@PathVariable Long id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Review review = reviewService.findReviewById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reseña no encontrada."));

        if (!review.getUser().getName().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar esta reseña.");
        }

        Game game = review.getGame();

        model.addAttribute("review", review);
        // CORREGIDO: Usamos "currentGame"
        model.addAttribute("currentGame", game);

        return "review-form";
    }

    /**
     * Procesa el envío del formulario para actualizar una reseña.
     */
    @PostMapping("/editar/{id}")
    public String updateReview(@PathVariable Long id,
                               @ModelAttribute("review") Review updatedReview,
                               BindingResult result,
                               Model model,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        // Recuperamos el juego completo para tener el SLUG y el NOMBRE en caso de error
        // El objeto updatedReview solo tiene el ID del juego (del input hidden)
        Long gameId = (updatedReview.getGame() != null) ? updatedReview.getGame().getId() : null;
        Game game = null;
        if (gameId != null) {
             game = gameService.findById(gameId).orElse(null);
        }

        if (result.hasErrors()) {
            if (game != null) {
                model.addAttribute("currentGame", game);
            }
            return "review-form";
        }

        try {
            Review existingReview = reviewService.findReviewById(id)
                    .orElseThrow(() -> new NoSuchElementException("Reseña no encontrada."));

            if (!existingReview.getUser().getName().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso.");
            }

            // Actualizamos campos
            existingReview.setTitle(updatedReview.getTitle());
            existingReview.setContent(updatedReview.getContent());
            existingReview.setRating(updatedReview.getRating());
            existingReview.setStatus(ReviewStatus.PENDING);
            existingReview.setReviewedBy(null);
            existingReview.setReviewNote(null);

            reviewService.updateReview(existingReview);

            redirectAttributes.addFlashAttribute("successMessage", "¡Reseña actualizada!");
            return "redirect:/juego/" + existingReview.getGame().getSlug();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Usamos 'game' que cargamos al principio para asegurar que tenemos el slug
            if (game != null) {
                return "redirect:/juego/" + game.getSlug();
            }
            return "redirect:/";
        }
    }
}