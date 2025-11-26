package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.RefreshToken;
import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.persistence.repository.RefreshTokenRepository;
import com.habitora.backend.service.interfaces.IRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String createRefreshToken(Usuario usuario, long expirationSeconds) {
        // generate raw token
        byte[] random = new byte[64];
        secureRandom.nextBytes(random);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(random);

        String tokenHash = hash(rawToken);

        RefreshToken rt = RefreshToken.builder()
                .tokenHash(tokenHash)
                .usuario(usuario)
                .createdAt(Instant.now())
                .expiryDate(Instant.now().plusSeconds(expirationSeconds))
                .build();

        refreshTokenRepository.save(rt);
        return rawToken;
    }

    @Override
    public Usuario validateRefreshToken(String rawToken) {
        String tokenHash = hash(rawToken);
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
                .map(RefreshToken::getUsuario)
                .orElse(null);
    }

    @Override
    public String validateAndRotate(String rawToken, long expirationSeconds) {
        String tokenHash = hash(rawToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElse(null);

        if (existing == null || existing.getExpiryDate().isBefore(Instant.now())) {
            return null;
        }

        Usuario usuario = existing.getUsuario();

        // delete only the existing token (rotate)
        refreshTokenRepository.deleteByTokenHash(tokenHash);

        // create a new refresh token for the same user
        String newRaw = createRefreshToken(usuario, expirationSeconds);
        return newRaw;
    }

    @Override
    public void deleteByTokenHash(String tokenHash) {
        refreshTokenRepository.deleteByTokenHash(tokenHash);
    }

    @Override
    public void deleteByRawToken(String rawToken) {
        String tokenHash = hash(rawToken);
        refreshTokenRepository.deleteByTokenHash(tokenHash);
    }

    @Override
    public void deleteByUsuario(Usuario usuario) {
        refreshTokenRepository.deleteByUsuario(usuario);
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
