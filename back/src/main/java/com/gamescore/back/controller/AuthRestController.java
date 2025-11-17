package com.gamescore.back.controller;

import com.gamescore.back.dto.UserRegistrationDto;
import com.gamescore.back.model.User;
import com.gamescore.back.model.enums.AuthProvider;
import com.gamescore.back.model.enums.Role;
import com.gamescore.back.security.jwt.JwtTokenProvider;
import com.gamescore.back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestParam String username, @RequestParam String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("message", "Login successful");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Validated @RequestBody UserRegistrationDto registrationDto) {
        if (userService.emailExists(registrationDto.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        User user = User.builder()
                .name(registrationDto.getUsername())
                .email(registrationDto.getEmail())
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();

        userService.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully!");

        return ResponseEntity.ok(response);
    }
}
