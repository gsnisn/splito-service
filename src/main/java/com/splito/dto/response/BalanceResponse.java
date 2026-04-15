package com.splito.dto.response;

import java.math.BigDecimal;

public record BalanceResponse(
        Long userId,
        BigDecimal balance
) {}
