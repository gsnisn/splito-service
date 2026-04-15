package com.splito.controller;

import com.splito.dto.mapper.ExpenseMapper;
import com.splito.dto.request.CreateExpenseRequest;
import com.splito.dto.response.ExpenseResponse;
import com.splito.exception.ResourceNotFoundException;
import com.splito.repository.ExpenseRepository;
import com.splito.repository.GroupRepository;
import com.splito.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;

    @PostMapping
    public ExpenseResponse add(@PathVariable("groupId") Long groupId,
                               @Valid @RequestBody CreateExpenseRequest req) {
        req.setGroupId(groupId);
        return expenseMapper.toResponse(expenseService.addExpense(req));
    }

    @GetMapping
    public List<ExpenseResponse> list(@PathVariable("groupId") Long groupId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        return expenseRepository.findByGroupId(groupId)
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    @GetMapping("/{expenseId}")
    public ExpenseResponse get(@PathVariable("groupId") Long groupId,
                               @PathVariable("expenseId") Long expenseId) {
        var e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (e.getGroup() == null || !Objects.equals(e.getGroup().getId(), groupId)) {
            throw new ResourceNotFoundException("Expense not found in this group");
        }
        return expenseMapper.toResponse(e);
    }

    @PutMapping("/{expenseId}")
    public ExpenseResponse update(@PathVariable("groupId") Long groupId,
                                  @PathVariable("expenseId") Long expenseId,
                                  @Valid @RequestBody CreateExpenseRequest req) {
        req.setGroupId(groupId);
        return expenseMapper.toResponse(expenseService.updateExpense(groupId, expenseId, req));
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("groupId") Long groupId,
                       @PathVariable("expenseId") Long expenseId) {
        expenseService.deleteExpense(groupId, expenseId);
    }
}