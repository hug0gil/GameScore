package com.gamescore.back.config;

import com.gamescore.back.security.CustomOAuth2UserService;
import com.gamescore.back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/registro", "/olvide-password", "/cambiar-password",
                                "/error", "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        .requestMatchers("/perfil/**", "/foro/nueva-resena/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll())

                // ============================================================
                // CONFIGURACIÓN DEL LOGIN FORMULARIO (Para tu HTML)
                // ============================================================
                .formLogin(form -> form
                        .loginPage("/login") // Muestra tu HTML cuando se requiere login
                        .loginProcessingUrl("/login") // INTERCEPTA el POST de tu <form th:action="@{/login}">
                        .defaultSuccessUrl("/", true) // Si el login es correcto, va al inicio
                        .failureUrl("/login?error") // Si falla, recarga la página y tu HTML muestra el <div
                                                    // th:if="${param.error}">
                        .usernameParameter("username") // Coincide con <input name="username"> de tu HTML
                        .passwordParameter("password") // Coincide con <input name="password"> de tu HTML
                        .permitAll())

                // ============================================================
                // OAUTH2 (Google/Discord)
                // ============================================================
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)))

                // ============================================================
                // LOGOUT
                // ============================================================
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout") // Tu Controller captura el param "logout" y muestra el
                                                           // mensaje
                        .permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService); // Conecta tu UserService
        authProvider.setPasswordEncoder(AppConfig.passwordEncoder()); // Conecta BCrypt
        return authProvider;
    }
}