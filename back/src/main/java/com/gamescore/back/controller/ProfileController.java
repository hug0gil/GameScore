package com.gamescore.back.controller;

// IMPORTS DE MODELOS Y REPOSITORIOS
import com.gamescore.back.model.User;
import com.gamescore.back.repository.UserRepository;
import com.gamescore.back.security.CustomOAuth2User; // Asegúrate que este paquete es correcto
import com.gamescore.back.service.EmailService;

// IMPORTS DE SPRING Y LOGGING
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // ========================================================================
    // 1. VER PERFIL
    // ========================================================================
    @GetMapping("/perfil")
    public String profile(Model model, @AuthenticationPrincipal Object principal) {
        try {
            User user = getAuthenticatedUser(principal);
            model.addAttribute("user", user);
            return "profile";
        } catch (Exception e) {
            log.error("Error cargando perfil", e);
            return "redirect:/login";
        }
    }

    // ========================================================================
    // 2. MOSTRAR FORMULARIO DE EDICIÓN
    // ========================================================================
    @GetMapping("/perfil/editar")
    public String editProfile(Model model, @AuthenticationPrincipal Object principal) {
        try {
            User user = getAuthenticatedUser(principal);
            model.addAttribute("user", user);
            return "edit-profile";
        } catch (Exception e) {
            return "redirect:/perfil";
        }
    }

    // ========================================================================
    // 3. PROCESAR ACTUALIZACIÓN DE PERFIL
    // ========================================================================
    @PostMapping("/perfil/actualizar")
    public String updateProfile(@ModelAttribute User formUser, 
                                @AuthenticationPrincipal Object principal, 
                                RedirectAttributes redirectAttributes) {
        try {
            // 1. Obtenemos el usuario REAL de la base de datos
            User currentUser = getAuthenticatedUser(principal);

            // 2. Actualizamos solo si hay datos
            if (formUser.getName() != null && !formUser.getName().isBlank()) {
                currentUser.setName(formUser.getName());
            }
            
            if (formUser.getAvatarUrl() != null && !formUser.getAvatarUrl().isBlank()) {
                currentUser.setAvatarUrl(formUser.getAvatarUrl());
            }

            // 3. Guardamos
            userRepository.save(currentUser);
            log.info("Perfil actualizado para el usuario: {}", currentUser.getEmail());

            redirectAttributes.addFlashAttribute("successMessage", "¡Perfil actualizado correctamente!");
        } catch (Exception e) {
            log.error("Error actualizando perfil", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar: " + e.getMessage());
        }

        return "redirect:/perfil";
    }

    // ========================================================================
    // 4. ENVIAR RESUMEN POR EMAIL
    // ========================================================================
    @PostMapping("/perfil/resumen")
    public String sendSummary(@AuthenticationPrincipal Object principal, RedirectAttributes redirectAttributes) {
        try {
            User user = getAuthenticatedUser(principal);
            emailService.sendWeeklySummary(user);
            redirectAttributes.addFlashAttribute("successMessage", "¡Resumen enviado! Revisa tu bandeja de entrada.");
        } catch (Exception e) {
            log.error("Error enviando resumen", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al enviar el correo.");
        }
        return "redirect:/perfil";
    }

    // ========================================================================
    // MÉTODOS PRIVADOS / AUXILIARES (Aquí estaba tu error)
    // ========================================================================
    
    /**
     * Este método extrae el email del "principal" (sea Google o Local)
     * y busca el usuario completo en la base de datos.
     */
    private User getAuthenticatedUser(Object principal) {
        String email = null;

        if (principal == null) {
            throw new IllegalStateException("No hay usuario autenticado en la sesión.");
        }

        // Caso 1: Login Social (Google, GitHub, etc.)
        if (principal instanceof CustomOAuth2User) {
            email = ((CustomOAuth2User) principal).getEmail();
        } 
        // Caso 2: Login Local (Usuario/Contraseña)
        else if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        }

        if (email == null) {
            throw new IllegalStateException("No se pudo identificar el email del usuario.");
        }

        // Buscamos en BD y si no existe lanzamos error
        String finalEmail = email;
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado en BD con email: " + finalEmail));
    }
}