package com.gamescore.back.config;

import com.gamescore.back.model.User;
import com.gamescore.back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class LoginListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        String email = null;

        // Obtener email según si es Google o Local
        if (principal instanceof OAuth2User) {
            email = ((OAuth2User) principal).getAttribute("email");
        } else if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        }

        // Actualizar contador en BD
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setLoginCount(user.getLoginCount() + 1);
                user.setLastLogin(java.time.LocalDateTime.now());
                userRepository.save(user);
            });
        }
    }
}