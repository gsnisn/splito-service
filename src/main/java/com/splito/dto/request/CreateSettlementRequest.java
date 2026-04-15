package com.splito.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreateSettlementRequest {

    @NotNull(message = "fromUserId is required")
    private Long fromUserId;

    @NotNull(message = "toUserId is required")
    private Long toUserId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be > 0")
    private BigDecimal amount;
}
