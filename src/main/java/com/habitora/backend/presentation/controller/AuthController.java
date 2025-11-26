package com.habitora.backend.presentation.controller;

import com.habitora.backend.presentation.dto.auth.request.LoginRequest;
import com.habitora.backend.presentation.dto.auth.request.RegisterRequest;
import com.habitora.backend.presentation.dto.auth.response.AuthResponse;
import com.habitora.backend.service.interfaces.IAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                           HttpServletResponse response) {
        String accessToken = authService.register(request, response);
        return ResponseEntity.ok(new AuthResponse(accessToken));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                        HttpServletResponse response) {
        String accessToken = authService.login(request, response);
        return ResponseEntity.ok(new AuthResponse(accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok("Sesión cerrada");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String newAccessToken = authService.refresh(request, response);
        return ResponseEntity.ok(new AuthResponse(newAccessToken));
    }

}
