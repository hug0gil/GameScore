package com.gamescore.back.controller;

import com.gamescore.back.model.User;
import com.gamescore.back.repository.UserRepository;
import com.gamescore.back.security.CustomOAuth2User;
import com.gamescore.back.service.EmailService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class ProfileController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/perfil")
    public String profile(Model model, @AuthenticationPrincipal(errorOnInvalidType = false) Object principal) {

        log.info("--- INICIANDO DEPURACIÓN DE /perfil ---");

        if (principal == null) {
            log.error("¡ERROR GRAVE! El 'principal' es NULO. Usuario no autenticado.");
            return "redirect:/login";
        }

        log.info("Clase del principal recibido: {}", principal.getClass().getName());

        // CASO 1: Login con OAuth2 (Google, Discord, etc.)
        if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;
            log.info("El principal es 'CustomOAuth2User'. Email: {}", customOAuth2User.getEmail());

            // Asumiendo que CustomOAuth2User ya tiene la entidad User mapeada
            model.addAttribute("user", customOAuth2User.getUser());

            return "profile";
        }

        // CASO 2: Login Local (Formulario con UserDetails estándar de Spring)
        else if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String email = userDetails.getUsername(); // En Spring Security, username suele ser el email
            log.info("El principal es 'UserDetails'. Email detectado: {}", email);

            // Buscamos la entidad completa en BD para tener avatar, fecha creación, etc.
            User user = userRepository.findByEmail(email)
                    .orElseThrow(
                            () -> new IllegalStateException("Usuario autenticado pero no encontrado en BD: " + email));

            model.addAttribute("user", user);

            return "profile";
        }

        // CASO ERROR: Tipo desconocido
        else {
            log.error("¡ERROR DE TIPO! El principal no es reconocido: '{}'", principal.getClass().getName());
            throw new IllegalStateException("Tipo de principal no soportado: " + principal.getClass().getName());
        }
    }

    @PostMapping("/perfil/resumen")
    public String sendSummary(@AuthenticationPrincipal Object principal, RedirectAttributes redirectAttributes) {
        // --- LOG DE DEPURACIÓN ---
        log.info(">>> SE HA RECIBIDO UNA PETICIÓN POST A /perfil/resumen <<<");

        String email = null;

        try {
            if (principal instanceof CustomOAuth2User) {
                email = ((CustomOAuth2User) principal).getEmail();
            } else if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            }

            // Más logs para ver qué pasa
            log.info("Intentando enviar correo a: {}", email);

            if (email != null) {
                User user = userRepository.findByEmail(email).orElseThrow();
                emailService.sendWeeklySummary(user);

                log.info(">>> ÉXITO: Correo enviado correctamente <<<");
                redirectAttributes.addFlashAttribute("successMessage",
                        "¡Resumen enviado! Revisa tu bandeja de entrada.");
            } else {
                log.error(">>> ERROR: No se pudo identificar el email del usuario <<<");
                redirectAttributes.addFlashAttribute("errorMessage", "No se pudo identificar tu usuario.");
            }
        } catch (Exception e) {
            // Loguear la excepción completa para verla en consola
            log.error(">>> EXCEPCIÓN ENVIANDO EMAIL: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al enviar: " + e.getMessage());
        }

        return "redirect:/perfil";
    }
}