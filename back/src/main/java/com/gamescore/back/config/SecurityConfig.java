package com.gamescore.back.config;

import com.gamescore.back.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas que cualquiera puede ver
                .requestMatchers(
                    "/",
                    "/juegos",
                    "/juego/**",
                    "/login",       // La página que mostrará los botones de OAuth2
                    "/registro",    // Página de registro
                    "/api/auth/**", // Endpoints de autenticación
                    "/error",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()

                // Cualquier otra ruta requiere que el usuario esté autenticado
                .anyRequest().authenticated()
            )
            // Configura el login con OAuth2
            .oauth2Login(oauth2 -> oauth2
                // Especifica que nuestra página en /login es donde empieza el flujo
                .loginPage("/login")
                // A dónde ir después de un login exitoso
                .defaultSuccessUrl("/perfil", true)
            )
            // Configura el logout
            .logout(logout -> logout
                .logoutSuccessUrl("/?logout") // Redirige a la página de inicio con un mensaje
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
