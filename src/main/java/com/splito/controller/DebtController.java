// =========================
// DebtController.java
// =========================
package com.splito.controller;

import com.splito.exception.ApiError;
import com.splito.exception.ResourceNotFoundException;
import com.splito.model.Expense;
import com.splito.model.Settlement;
import com.splito.model.SplitoGroup;
import com.splito.repository.ExpenseRepository;
import com.splito.repository.GroupRepository;
import com.splito.repository.SettlementRepository;
import com.splito.service.BalanceService;
import com.splito.service.DebtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final SettlementRepository settlementRepository;
    private final BalanceService balanceService;

    @Operation(summary = "Get simplified debt transactions for a group")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions calculated"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Group not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @GetMapping("/{groupId}/debts")
    public List<DebtService.Transaction> getSimplifiedDebts(@PathVariable Long groupId) {

        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);

        Map<Long, BigDecimal> balances =
                balanceService.calculateGroupBalances(expenses, settlements);

        return debtService.simplifyDebts(balances);
    }

}
