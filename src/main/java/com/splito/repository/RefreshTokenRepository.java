package com.splito.repository;

import com.splito.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    long deleteByExpiresAtBefore(Instant now);

    @Modifying
    @Query("""
        update RefreshToken rt
           set rt.revokedAt = :now
         where rt.user.id = :userId
           and rt.revokedAt is null
    """)
    int revokeAllActiveForUser(@Param("userId") Long userId, @Param("now") Instant now);
}
