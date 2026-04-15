package com.splito.controller;

import com.splito.dto.mapper.ExpenseMapper;
import com.splito.dto.request.CreateDirectExpenseRequest;
import com.splito.dto.request.CreateExpenseRequest;
import com.splito.dto.response.ExpenseResponse;
import com.splito.model.SplitoGroup;
import com.splito.service.DirectGroupService;
import com.splito.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/direct-expenses")
@RequiredArgsConstructor
public class DirectExpenseController {

    private final DirectGroupService directGroupService;
    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;

    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody CreateDirectExpenseRequest req) {

        if (req.getPaidByUserId().equals(req.getOtherUserId())) {
            throw new IllegalArgumentException("paidByUserId and otherUserId must be different");
        }

        SplitoGroup g = directGroupService.getOrCreateDirectGroup(req.getPaidByUserId(), req.getOtherUserId());

        CreateExpenseRequest er = new CreateExpenseRequest();
        er.setDescription(req.getDescription());
        er.setAmount(req.getAmount());
        er.setPaidByUserId(req.getPaidByUserId());
        er.setGroupId(g.getId());
        er.setSplitBetweenUserIds(List.of(req.getPaidByUserId(), req.getOtherUserId()));
        er.setExactSplits(req.getExactSplits());

        return expenseMapper.toResponse(expenseService.addExpense(er));
    }
}
