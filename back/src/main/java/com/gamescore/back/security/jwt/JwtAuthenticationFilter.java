package com.gamescore.back.security.jwt;

import com.gamescore.back.model.User;
import com.gamescore.back.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            // Se usa un enfoque funcional para validar y procesar el token.
            getJwtFromRequest(request)
                .filter(jwtTokenProvider::validateToken) // Continúa solo si el token es válido.
                .flatMap(this::getUserFromToken)         // Obtiene el usuario si el token es válido.
                .ifPresent(user -> {                     /* Si se encontró un usuario válido y habilitado,
                                                         // se configura la autenticación en el contexto de seguridad.*/
                    UsernamePasswordAuthenticationToken authentication = createAuthentication(user, request);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Usuario autenticado via JWT: {}", user.getEmail());
                });
        } catch (Exception ex) {
            log.error("Error al configurar la autenticación del usuario a través de JWT.", ex);
        }
        
        // El filtro siempre debe continuar la cadena.
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token "Bearer" del encabezado de autorización.
     * Devuelve un Optional para un manejo más seguro y funcional.
     */
    private Optional<String> getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }

    /**
     * A partir de un token JWT, obtiene el email, busca al usuario en la base de datos
     * y verifica que esté habilitado.
     */
    private Optional<User> getUserFromToken(String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        // Verifica si el usuario existe y si está habilitado.
        if (userOptional.isPresent() && !userOptional.get().getEnabled()) {
            log.warn("Intento de acceso de un usuario deshabilitado: {}", email);
            return Optional.empty(); // Si está deshabilitado, se trata como si no existiera.
        }
        
        return userOptional;
    }

    /**
     * Crea un objeto de autenticación para el usuario especificado.
     */
    private UsernamePasswordAuthenticationToken createAuthentication(User user, HttpServletRequest request) {
        var authorities = Collections.singletonList(user.getRole().asGrantedAuthority());
        
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            user, null, authorities
        );
        
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}