package com.splito.dto.response;

import java.util.List;

public record GroupBalanceResponse(
        Long groupId,
        List<BalanceResponse> balances
) {}
