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
     * GET /reviews/nueva?gameId={id}
     */
    @GetMapping("/nueva")
    public String showNewReviewForm(@RequestParam Long gameId, Model model, Principal principal) {
        // La seguridad de Spring debería manejar esto, pero verificamos.
        if (principal == null) {
            return "redirect:/login"; // Redirige a login si no está autenticado
        }

        // 1. Cargar el juego
        Game game = gameService.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Juego no encontrado con ID: " + gameId));

        // 2. Crear una nueva Review para el binding
        Review newReview = new Review();
        
        // 3. Establecer el objeto Game con solo el ID (para el campo oculto del formulario POST)
        // Esto es esencial para el binding.
        newReview.setGame(game);

        // 4. Pasar objetos al modelo
        model.addAttribute("review", newReview);
        // Añadimos el objeto 'game' explícitamente para los detalles en la cabecera del formulario (review-form.html)
        model.addAttribute("game", game);

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
        
        // La validación de BindingResult (errores de formulario) y de campos (si usas @Valid) debe ir aquí.
        if (result.hasErrors()) {
            // Si hay errores, necesitas volver a cargar el objeto Game completo para la vista.
            try {
                Game game = gameService.findById(review.getGame().getId())
                        .orElseThrow(() -> new NoSuchElementException("Juego no encontrado para la reseña."));
                redirectAttributes.addFlashAttribute("game", game);
            } catch (NoSuchElementException ignored) {
                // Si el juego no existe, simplemente redirigimos a '/' al final.
            }
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.review", result);
            redirectAttributes.addFlashAttribute("review", review);
            // La redirección a /reviews/nueva no funciona bien con FlashAttributes. Mejor retornar la vista directamente
            // Si quieres que funcione con redirect, deberías usar la ruta del juego para el redirect final.
            // Para simplicidad, asumo que usas la redirección en caso de éxito y manejas los errores en la misma página con Thymeleaf.
            // Si quieres que los errores vuelvan a la página del formulario, necesitarías recargar el modelo.
            return "review-form";
        }

        try {
            // 1. Buscar el objeto User completo por el username del usuario logueado
            User loggedInUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new NoSuchElementException("Usuario logueado no encontrado."));

            // 2. Asignar el User completo a la reseña
            review.setUser(loggedInUser);

            // 3. Obtener el objeto Game completo (solo tiene el ID del form)
            Game game = gameService.findById(review.getGame().getId())
                    .orElseThrow(() -> new NoSuchElementException("Juego no encontrado para la reseña."));
            review.setGame(game);

            // 4. Guardar la reseña
            Review savedReview = reviewService.createReview(review);

            redirectAttributes.addFlashAttribute("successMessage", "¡Reseña creada y enviada para moderación!");
            
            // Redirigir a la página de detalle del juego
            return "redirect:/juego/" + game.getSlug();

        } catch (NoSuchElementException | IllegalArgumentException e) {
            // Captura errores de juego/usuario no encontrado o reseña duplicada
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            
            // Intenta redirigir a la página del juego si tenemos el slug
            String slug = (review.getGame() != null && review.getGame().getSlug() != null)
                    ? review.getGame().getSlug()
                    : "";
            return "redirect:/juego/" + slug;
        }
    }

    /**
     * Muestra el formulario para editar una reseña existente.
     * GET /reviews/editar/{id}
     */
    @GetMapping("/editar/{id}")
    public String showEditReviewForm(@PathVariable Long id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        // 1. Cargar la reseña
        Review review = reviewService.findReviewById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reseña no encontrada."));

        // 2. Verificar que el usuario logueado sea el autor de la reseña
        if (!review.getUser().getName().equals(principal.getName())) {
            // Si no es el autor, lanzar 403 Forbidden o redirigir con error
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar esta reseña.");
        }
        
        // Opcional: Impedir editar reseñas ya aprobadas o rechazadas (solo PENDING)
        // if (review.getStatus() != ReviewStatus.PENDING) {
        //     throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes editar reseñas pendientes de moderación.");
        // }


        // 3. Cargar el juego asociado
        Game game = review.getGame();
        
        // 4. Pasar objetos al modelo
        model.addAttribute("review", review);
        model.addAttribute("game", game);

        return "review-form";
    }

    /**
     * Procesa el envío del formulario para actualizar una reseña existente.
     * POST /reviews/editar/{id}
     */
    @PostMapping("/editar/{id}")
    public String updateReview(@PathVariable Long id, 
            @ModelAttribute("review") Review updatedReview,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        if (principal == null) {
            return "redirect:/login";
        }
        
        // La validación de BindingResult (si usas @Valid) debe ir aquí.
        if (result.hasErrors()) {
            // Si hay errores, retornar la vista del formulario
            return "review-form";
        }

        try {
            // 1. Cargar la reseña existente para verificar la autorización y obtener datos completos
            Review existingReview = reviewService.findReviewById(id)
                .orElseThrow(() -> new NoSuchElementException("Reseña no encontrada."));

            // 2. Verificar que el usuario logueado sea el autor de la reseña
            if (!existingReview.getUser().getName().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar esta reseña.");
            }
            
            // 3. Actualizar los campos modificables
            existingReview.setTitle(updatedReview.getTitle());
            existingReview.setContent(updatedReview.getContent());
            existingReview.setRating(updatedReview.getRating());
            
            // 4. Re-establecer el estado a PENDING para que pase por moderación de nuevo
            existingReview.setStatus(ReviewStatus.PENDING);
            existingReview.setReviewedBy(null);
            existingReview.setReviewNote(null);
            
            // 5. Guardar la reseña actualizada
            Review savedReview = reviewService.updateReview(existingReview); // Asumo que updateReview existe en ReviewService

            redirectAttributes.addFlashAttribute("successMessage", "¡Reseña actualizada y reenviada para moderación!");
            
            // Redirigir a la página de detalle del juego (usamos el slug del juego original)
            return "redirect:/juego/" + existingReview.getGame().getSlug();

        } catch (NoSuchElementException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            
            // Intenta redirigir a la página del juego si tenemos el slug (usamos el slug del juego original si es posible)
            String slug = (updatedReview.getGame() != null && updatedReview.getGame().getSlug() != null)
                    ? updatedReview.getGame().getSlug()
                    : "";
            return "redirect:/juego/" + slug;
        }
    }
}