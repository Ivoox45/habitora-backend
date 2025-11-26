package com.habitora.backend.service.interfaces;

import com.habitora.backend.persistence.entity.Usuario;

public interface IRefreshTokenService {
    /**
     * Create and persist a new refresh token for the user and return the raw token.
     */
    String createRefreshToken(Usuario usuario, long expirationSeconds);

    /**
     * Validate the raw token and return the associated user if valid, otherwise null.
     */
    Usuario validateRefreshToken(String rawToken);

    /**
     * Validate and rotate the given raw token: if valid, delete the old token and
     * create a new one, returning the new raw token.
     */
    String validateAndRotate(String rawToken, long expirationSeconds);

    /**
     * Delete all refresh tokens for the given user (used on logout all devices).
     */
    void deleteByUsuario(Usuario usuario);

    /**
     * Delete a refresh token by its raw hash (used on logout of current device).
     */
    void deleteByTokenHash(String tokenHash);

    /**
     * Delete a refresh token by providing the raw token (will be hashed internally).
     */
    void deleteByRawToken(String rawToken);
}
