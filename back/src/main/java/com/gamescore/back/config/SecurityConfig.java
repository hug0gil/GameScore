package com.gamescore.back.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.gamescore.back.security.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Configuración básica de CORS y CSRF
                                .cors(cors -> cors.configure(http))
                                .csrf(csrf -> csrf.disable())

                                // Autorización de endpoints
                                .authorizeHttpRequests(auth -> auth
                                                // Endpoints públicos
                                                .requestMatchers(
                                                                "/api/public/**",
                                                                "/api/auth/**",
                                                                "/oauth2/**",
                                                                "/login/**",
                                                                "/login.html")
                                                .permitAll()

                                                // Solo ADMIN
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                                // USER o ADMIN
                                                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                                                // Todo lo demás requiere autenticación
                                                .anyRequest().authenticated())

                                // Login con formulario (opcional)
                                .formLogin(form -> form
                                                .loginPage("/login") // Puedes cambiarlo por tu propia vista de login
                                                .permitAll())

                                // Login con OAuth2
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService)));

                return http.build();
        }
}
