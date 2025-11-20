package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.service.interfaces.IJwtService;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements IJwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ==========================
    //     PUBLIC METHODS
    // ==========================

    @Override
    public String generateAccessToken(Usuario usuario) {
        return buildToken(usuario, accessTokenExpirationMs);
    }

    @Override
    public String generateRefreshToken(Usuario usuario) {
        return buildToken(usuario, refreshTokenExpirationMs);
    }

    @Override
    public String extractUserEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token, Usuario usuario) {
        final String email = extractUserEmail(token);
        return (email.equals(usuario.getEmail()) && !isTokenExpired(token));
    }

    @Override
    public boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    // ==========================
    //     PRIVATE HELPERS
    // ==========================

    private String buildToken(Usuario usuario, long expirationMs) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("id", usuario.getId());
    claims.put("nombre", usuario.getNombreCompleto());

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(usuario.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
}


    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims debugExtractClaims(String token) {
    return extractAllClaims(token);
}

}
