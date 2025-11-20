package com.habitora.backend.service.interfaces;

import com.habitora.backend.persistence.entity.Usuario;

import io.jsonwebtoken.Claims;

public interface IJwtService {

    String generateAccessToken(Usuario usuario);

    String generateRefreshToken(Usuario usuario);

    String extractUserEmail(String token);

    boolean isTokenValid(String token, Usuario usuario);

    boolean isTokenExpired(String token);

    public Claims debugExtractClaims(String token);
}
