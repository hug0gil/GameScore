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
                                .csrf(csrf -> csrf.disable()) // <-- AGREGA ESTA LÍNEA

                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/", "/login", "/registro", "/olvide-password",
                                                                "/cambiar-password",
                                                                "/error", "/css/**", "/js/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers("/perfil/**", "/foro/nueva-resena/**").authenticated()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().permitAll())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/", true)
                                                .failureUrl("/login?error")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .permitAll())

                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService)))

                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
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