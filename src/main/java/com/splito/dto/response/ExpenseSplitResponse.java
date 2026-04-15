package com.splito.dto.response;

import java.math.BigDecimal;

public record ExpenseSplitResponse(
        Long userId,
        BigDecimal amount
) {}
