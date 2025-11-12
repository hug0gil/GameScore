package com.gamescore.back.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    
    // 1. Inyección de propiedades desde application.properties
    @Value("${jwt.secret}")
    private String jwtSecretString;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;
    
    private SecretKey jwtSecretKey;

    // 2. Inicializar la clave secreta una sola vez después de la construcción del bean
    @PostConstruct
    protected void init() {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes(StandardCharsets.UTF_8));
    }
    
    // 3. Método unificado para generar tokens
    public String generateToken(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede ser nulo o vacío para generar un token");
        }
        
        Instant now = Instant.now();
        Instant expiryDate = now.plus(jwtExpirationInMs, ChronoUnit.MILLIS);
        
        log.debug("Generando token JWT para: {}", email);
        
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now)) // La API de JWT todavía usa Date, así que convertimos.
                .expiration(Date.from(expiryDate))
                .signWith(jwtSecretKey)
                .compact();
    }
    
    // 4. Sobrecarga para generar token desde un objeto Authentication (más conveniente)
    public String generateToken(Authentication authentication) {
        // Extrae el email del "principal" del objeto Authentication.
        // Esto funciona tanto para OAuth2User como para UserDetails.
        String email;
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User) {
            email = ((OAuth2User) principal).getAttribute("email");
        } else if (principal instanceof com.gamescore.back.model.User) {
            email = ((com.gamescore.back.model.User) principal).getEmail();
        } else {
            email = principal.toString();
        }
        
        return generateToken(email);
    }
    
    // 5. Método para obtener el email del token
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
        return claims.getSubject();
    }
    
    // 6. Método de validación del token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token);
            
            return true;
            
        } catch (SignatureException ex) {
            log.error("Firma JWT inválida: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Token JWT malformado: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Token JWT no soportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("El string de claims del JWT está vacío: {}", ex.getMessage());
        }
        
        return false;
    }
}