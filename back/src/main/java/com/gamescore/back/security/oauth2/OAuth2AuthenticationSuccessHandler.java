package com.gamescore.back.security.oauth2;

import com.gamescore.back.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${app.oauth2.redirect-uri:http://localhost:8080/test-oauth.html}")
    private String redirectUri;
    
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        
        log.info("OAuth2 login exitoso para: {}", principal.getEmail());
        
        // Generar JWT
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Construir URL de redirección a Angular con el token
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build()
                .toUriString();
        
        log.debug("Redirigiendo a Angular: {}", targetUrl);
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}