package com.splito.service;

import com.splito.exception.TokenReuseException;
import com.splito.exception.UnauthorizedException;
import com.splito.model.RefreshToken;
import com.splito.model.SplitoUser;
import com.splito.repository.RefreshTokenRepository;
import com.splito.security.TokenHashing;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${spring.security.jwt.refresh-ttl-days}")
    private long refreshTtlDays;

    public record RefreshTokenPair(String rawToken, RefreshToken entity) {}

    @Transactional
    public RefreshTokenPair issue(SplitoUser user, String device) {
        String raw = generateOpaqueToken();
        String hash = TokenHashing.sha256Hex(raw);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hash);
        rt.setDevice(device);
        rt.setExpiresAt(Instant.now().plusSeconds(refreshTtlDays * 24 * 60 * 60));
        rt.setRevokedAt(null);

        RefreshToken saved = refreshTokenRepository.save(rt);
        return new RefreshTokenPair(raw, saved);
    }

    @Transactional
    public RefreshToken verifyActive(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required"); // -> 400
        }

        String hash = TokenHashing.sha256Hex(rawToken);

        RefreshToken rt = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token")); // -> 401

        Instant now = Instant.now();

        if (rt.isRevoked()) throw new UnauthorizedException("Refresh token revoked"); // -> 401
        if (rt.isExpired(now)) throw new UnauthorizedException("Refresh token expired"); // -> 401

        return rt;
    }

    /**
     * Rotation: old token revoked, new token created.
     */
    @Transactional
    public RefreshTokenPair rotate(String rawToken, String device) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required"); // -> 400
        }

        String hash = TokenHashing.sha256Hex(rawToken);

        RefreshToken current = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token")); // -> 401

        Instant now = Instant.now();

        if (current.isExpired(now)) {
            throw new UnauthorizedException("Refresh token expired"); // -> 401
        }

        if (current.isRevoked()) {
            // 🔥 REUSE DETECTED: revoke every active refresh token for this user (logout all devices)
            refreshTokenRepository.revokeAllActiveForUser(current.getUser().getId(), now);
            throw new TokenReuseException("Refresh token reuse detected"); // -> 401
        }

        // Normal rotation
        current.setRevokedAt(now);
        refreshTokenRepository.save(current);

        return issue(current.getUser(), device);
    }


    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required"); // -> 400
        }

        String hash = TokenHashing.sha256Hex(rawToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
            if (!rt.isRevoked()) {
                rt.setRevokedAt(Instant.now());
                refreshTokenRepository.save(rt);
            }
        });
    }

    @Transactional
    public long purgeExpired() {
        return refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
