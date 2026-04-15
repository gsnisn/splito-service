package com.splito.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
public class CreateDirectExpenseRequest {

    @NotBlank(message = "description is required")
    @Size(max = 255, message = "description too long")
    private String description;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be > 0")
    private BigDecimal amount;

    @NotNull(message = "paidByUserId is required")
    private Long paidByUserId;

    @NotNull(message = "otherUserId is required")
    private Long otherUserId;

    @Valid
    private List<CreateExpenseRequest.SplitShare> exactSplits;
}
