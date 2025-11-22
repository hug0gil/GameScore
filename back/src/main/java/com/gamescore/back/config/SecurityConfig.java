package com.gamescore.back.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import com.gamescore.back.security.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        // 1. Inyecta el servicio correcto (del paquete 'security')
        private final CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(
                                                                "/", "/login", "/error", "/favicon.ico",
                                                                "/images/**", "/css/**", "/js/**", "/webjars/**")
                                                .permitAll()
                                                .requestMatchers("/perfil/**", "/foro/nueva-resena/**").authenticated()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                // Importante
                                                                // Le dice a Spring que use TU servicio para procesar el
                                                                // usuario de OAuth2.
                                                                .userService(customOAuth2UserService)))
                                // --- Logout (POST seguro) ---
                                .logout(logout -> logout
                                                .logoutUrl("/logout") // URL de logout
                                                .logoutSuccessUrl("/login?logout") // redirige aquí al cerrar sesión
                                                .permitAll());
                // ...
                return http.build();
        }
}

// --- Antiguo SecurityConfig ---
// package com.gamescore.back.config;

// import lombok.RequiredArgsConstructor;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import
// org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import
// org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import
// org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.web.SecurityFilterChain;

// import com.gamescore.back.security.CustomOAuth2UserService;

// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity
// @RequiredArgsConstructor
// public class SecurityConfig {

// private final CustomOAuth2UserService customOAuth2UserService;

// @Bean
// public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
// http
// // Configuración básica de CORS y CSRF
// .cors(cors -> cors.configure(http))
// .csrf(csrf -> csrf.disable())

// // Autorización de endpoints
// .authorizeHttpRequests(auth -> auth
// // Endpoints públicos
// .requestMatchers(
// "/api/public/**",
// "/api/auth/**",
// "/oauth2/**",
// "/login/**",
// "/login.html",
// "/images/**",
// "/css/**",
// "/js/**")
// .permitAll()

// // Solo ADMIN
// .requestMatchers("/api/admin/**").hasRole("ADMIN")

// // USER o ADMIN
// .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

// // Todo lo demás requiere autenticación
// .anyRequest().authenticated())

// // Login con formulario (opcional)
// .formLogin(form -> form
// .loginPage("/login") // Puedes cambiarlo por tu propia vista de login
// .permitAll())

// // Login con OAuth2
// .oauth2Login(oauth2 -> oauth2
// .userInfoEndpoint(userInfo -> userInfo
// .userService(customOAuth2UserService)));

// return http.build();
// }
// }
