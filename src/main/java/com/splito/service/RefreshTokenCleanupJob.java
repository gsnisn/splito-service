package com.splito.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenService refreshTokenService;

    // every day at 03:30
    @Scheduled(cron = "0 30 3 * * *")
    public void purgeExpiredDaily() {
        long deleted = refreshTokenService.purgeExpired();
        log.info("Purged expired refresh tokens: {}", deleted);
    }
}
