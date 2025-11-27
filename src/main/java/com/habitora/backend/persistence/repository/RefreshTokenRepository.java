package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.RefreshToken;
import com.habitora.backend.persistence.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    @org.springframework.data.jpa.repository.Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.usuario WHERE rt.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHash(@org.springframework.data.repository.query.Param("tokenHash") String tokenHash);
    
    void deleteByUsuario(Usuario usuario);
    void deleteByTokenHash(String tokenHash);

    // Delete all tokens that expired before given instant
    void deleteByExpiryDateBefore(java.time.Instant instant);
}
