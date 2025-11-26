package com.habitora.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupTask {

    private final com.habitora.backend.persistence.repository.RefreshTokenRepository refreshTokenRepository;

    // Runs every hour to remove expired refresh tokens
    @Scheduled(cron = "0 0 * * * *")
    public void removeExpiredTokens() {
        Instant now = Instant.now();
        try {
            int before = refreshTokenRepository.findAll().size();
            refreshTokenRepository.deleteByExpiryDateBefore(now);
            int after = refreshTokenRepository.findAll().size();
            log.info("RefreshTokenCleanupTask: removed {} expired tokens", (before - after));
        } catch (Exception e) {
            log.error("Error cleaning expired refresh tokens: {}", e.getMessage());
        }
    }
}
