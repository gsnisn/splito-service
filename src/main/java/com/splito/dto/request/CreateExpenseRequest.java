package com.splito.dto.request;

import com.splito.validation.ValidExpenseSplit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@ValidExpenseSplit   // 👈 class-level validation
public class CreateExpenseRequest {

    @NotBlank(message = "description is required")
    @Size(max = 255, message = "description too long")
    private String description;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be > 0")
    private BigDecimal amount;

    @NotNull(message = "paidByUserId is required")
    private Long paidByUserId;

    // set from controller path
    private Long groupId;

    // used for EQUAL split
    private List<@NotNull Long> splitBetweenUserIds;

    // used for EXACT split
    @Valid
    @Size(min = 1, message = "exactSplits must contain at least one entry")
    private List<SplitShare> exactSplits;


    @Getter @Setter
    public static class SplitShare {

        @NotNull(message = "exactSplits.userId is required")
        private Long userId;

        @NotNull(message = "exactSplits.amount is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "exactSplits.amount must be >= 0")
        private BigDecimal amount;
    }
}
