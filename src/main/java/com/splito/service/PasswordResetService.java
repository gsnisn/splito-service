package com.splito.service;

import com.splito.exception.UnauthorizedException;
import com.splito.model.PasswordResetToken;
import com.splito.model.SplitoUser;
import com.splito.repository.PasswordResetTokenRepository;
import com.splito.repository.RefreshTokenRepository;
import com.splito.repository.UserRepository;
import com.splito.security.TokenHashing;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom random = new SecureRandom();

    @Value("${spring.security.jwt.reset-ttl-minutes:15}")
    private long resetTtlMinutes;

    public record ResetTokenPair(String rawToken, PasswordResetToken entity) {}

    @Transactional
    public ResetTokenPair createResetTokenIfUserExists(String email) {

        return userRepository.findByEmail(email)
                .map(user -> {

                    // optional: keep only one active reset token per user
                    passwordResetTokenRepository.deleteByUserId(user.getId());

                    String raw = generateOpaqueToken();
                    String hash = TokenHashing.sha256Hex(raw);

                    PasswordResetToken prt = new PasswordResetToken();
                    prt.setUser(user);
                    prt.setTokenHash(hash);
                    prt.setExpiresAt(Instant.now().plusSeconds(resetTtlMinutes * 60));
                    prt.setUsedAt(null);

                    PasswordResetToken saved = passwordResetTokenRepository.save(prt);
                    return new ResetTokenPair(raw, saved);
                })
                .orElse(null);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {

        String hash = TokenHashing.sha256Hex(rawToken);

        PasswordResetToken prt = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired reset token"));

        Instant now = Instant.now();

        if (prt.isUsed() || prt.isExpired(now)) {
            throw new UnauthorizedException("Invalid or expired reset token");
        }

        SplitoUser user = prt.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsedAt(now);
        passwordResetTokenRepository.save(prt);

        // logout everywhere
        refreshTokenRepository.revokeAllActiveForUser(user.getId(), now);
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
