package com.splito.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

public record ExpenseResponse(
        Long id,
        String description,
        BigDecimal amount,
        Long groupId,
        Long paidByUserId,
        List<Long> splitBetweenUserIds,
        Instant createdAt
) {}
