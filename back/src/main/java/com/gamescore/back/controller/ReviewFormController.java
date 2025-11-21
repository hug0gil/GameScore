package com.gamescore.back.controller;

import com.gamescore.back.model.Review;
import com.gamescore.back.model.Game;
import com.gamescore.back.model.User;
import com.gamescore.back.service.ReviewService;
import com.gamescore.back.service.GameService;
import com.gamescore.back.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewFormController {

    private final ReviewService reviewService;
    private final GameService gameService;
    private final UserService userService;

    /**
     * Muestra el formulario para crear una nueva reseña, preseleccionando el juego.
     * Mapeo: GET /reviews/nueva?gameId={id}
     */
    @GetMapping("/nueva")
    public String showNewReviewForm(@RequestParam Long gameId, Model model, Authentication authentication) {

        // 1. Seguridad
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }

        // 2. Cargar Juego
        Game game = gameService.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado"));

        // 3. Cargar Usuario (Usando el nuevo método findByUsername)
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + authentication.getName()));

        // 4. Crear Reseña
        Review review = new Review();
        review.setGame(game);
        review.setUser(currentUser);

        // 5. Pasar al modelo
        model.addAttribute("review", review);
        model.addAttribute("currentGame", game);

        return "review-form";
    }

    /**
     * Procesa el envío del formulario para guardar una nueva reseña.
     * Mapeo: POST /reviews/nueva
     */
    @PostMapping("/nueva")
    @Transactional
    public String saveNewReview(@ModelAttribute("review") Review review,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        // 1. Cargar Usuario (Usando el nuevo método findByUsername)
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        review.setUser(currentUser);

        try {
            // 2. Guardar la reseña
            Review savedReview = reviewService.createReview(review);
            redirectAttributes.addFlashAttribute("successMessage", "¡Reseña enviada! Está pendiente de aprobación.");

            // 3. Redirigir al detalle del juego
            String redirectPath = "/juego/" + (savedReview.getGame().getSlug() != null ? savedReview.getGame().getSlug()
                    : savedReview.getGame().getId());
            return "redirect:" + redirectPath;
        } catch (IllegalArgumentException e) {
            // Manejar error de reseña duplicada
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            String redirectPath = "/juego/"
                    + (review.getGame().getSlug() != null ? review.getGame().getSlug() : review.getGame().getId());
            return "redirect:" + redirectPath;
        } catch (NoSuchElementException e) {
            // Manejar error si el juego no se encuentra por el ID
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}