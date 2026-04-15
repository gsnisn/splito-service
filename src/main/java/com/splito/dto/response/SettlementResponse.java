package com.splito.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

public record SettlementResponse(
        Long id,
        Long fromUserId,
        Long toUserId,
        BigDecimal amount,
        Instant createdAt
) {}
