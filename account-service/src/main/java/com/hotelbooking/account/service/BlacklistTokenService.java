package com.hotelbooking.account.service;

import com.hotelbooking.account.entity.TokenBlacklist;
import com.hotelbooking.account.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BlacklistTokenService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtService jwtService;

    /**
     * Add token to blacklist
     */
    @Transactional
    public void blacklistToken(String token, String userEmail) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        // Check if token is already blacklisted
        if (isTokenBlacklisted(token)) {
            return; // Already blacklisted, no need to add again
        }

        // Extract expiration date from token
        OffsetDateTime expiresAt = extractExpirationFromToken(token);
        
        TokenBlacklist blacklistedToken = new TokenBlacklist();
        blacklistedToken.setJwtToken(token);
        blacklistedToken.setExpiresAt(expiresAt);
        blacklistedToken.setUserEmail(userEmail);
        
        tokenBlacklistRepository.save(blacklistedToken);
    }

    /**
     * Check if a token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        return tokenBlacklistRepository.existsByJwtToken(token);
    }

    /**
     * Extract expiration date from JWT token
     */
    private OffsetDateTime extractExpirationFromToken(String token) {
        try {
            return jwtService.extractClaim(token, claims -> 
                OffsetDateTime.ofInstant(claims.getExpiration().toInstant(), 
                java.time.ZoneOffset.UTC));
        } catch (Exception e) {
            // If we can't extract expiration, set it to expire in 24 hours as fallback
            return OffsetDateTime.now().plusHours(24);
        }
    }

    /**
     * Cleanup expired tokens from blacklist
     * Runs every hour to clean up expired tokens
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredTokens() {
        tokenBlacklistRepository.deleteExpiredTokens(OffsetDateTime.now());
    }
}