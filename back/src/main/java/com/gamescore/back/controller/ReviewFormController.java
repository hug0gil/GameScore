package com.gamescore.back.controller;

import com.gamescore.back.model.Review;
import com.gamescore.back.model.Game;
import com.gamescore.back.model.User;
import com.gamescore.back.service.GameService;
import com.gamescore.back.service.ReviewService;
import com.gamescore.back.service.UserService; // Asume que tienes un UserService
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
    private final UserService userService; // ⬅️ IMPORTANTE: Necesitas inyectar un servicio para obtener el usuario

    /**
     * Muestra el formulario para crear una nueva reseña.
     * GET /reviews/nueva?gameId={id}
     */
    @GetMapping("/nueva")
    public String showNewReviewForm(@RequestParam Long gameId, Model model, Principal principal) {
        // La seguridad de Spring debería manejar esto, pero verificamos.
        if (principal == null) {
            return "redirect:/login";
        }

        // 1. Cargar el juego
        Game game = gameService.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Juego no encontrado con ID: " + gameId));

        // 2. Crear una nueva Review para el binding
        Review newReview = new Review();

        // 3. Establecer el objeto Game con solo el ID (para el campo oculto del
        // formulario)
        newReview.setGame(game);

        // 4. Pasar objetos al modelo
        model.addAttribute("review", newReview);

        return "review-form"; // Retorna la plantilla review-form.html
    }

    /**
     * Procesa el envío del formulario para crear una nueva reseña.
     * POST /reviews/nueva
     */
    @PostMapping("/nueva")
    public String createReview(@ModelAttribute("review") Review review,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        // --- Lógica Crítica de Seguridad y Usuario ⚠️ ---
        try {
            // 1. Buscar el objeto User completo por el username del usuario logueado
            User loggedInUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new NoSuchElementException("Usuario logueado no encontrado."));

            // 2. Asignar el User completo a la reseña
            review.setUser(loggedInUser);

            // 3. Asignar el objeto Game completo (solo tiene el ID del form)
            // Necesitamos el objeto Game completo para obtener el SLUG para la redirección
            Game game = gameService.findById(review.getGame().getId())
                    .orElseThrow(() -> new NoSuchElementException("Juego no encontrado para la reseña."));
            review.setGame(game);

            // 4. Guardar la reseña (la validación de duplicados está en ReviewService)
            Review savedReview = reviewService.createReview(review);

            redirectAttributes.addFlashAttribute("successMessage", "¡Reseña creada y enviada para moderación!");
            // Redirigir a la página de detalle del juego
            return "redirect:/juego/" + game.getSlug();

        } catch (NoSuchElementException | IllegalArgumentException e) {
            // Captura errores de juego/usuario no encontrado o reseña duplicada
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Si el error es sobre un juego no encontrado, redirige a '/' para evitar más
            // fallos.
            String slug = (review.getGame() != null && review.getGame().getSlug() != null)
                    ? review.getGame().getSlug()
                    : "";
            return "redirect:/juego/" + slug;
        }
    }
}