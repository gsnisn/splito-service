package com.splito.dto.mapper;

import com.splito.dto.response.ExpenseResponse;
import com.splito.model.Expense;
import com.splito.model.SplitoUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpenseMapper {

    public ExpenseResponse toResponse(Expense e) {
        if (e == null) return null;

        List<Long> splitIds = e.getSplitBetween() == null
                ? List.of()
                : e.getSplitBetween().stream().map(SplitoUser::getId).toList();

        return new ExpenseResponse(
                e.getId(),
                e.getDescription(),
                e.getAmount(),
                e.getGroup() == null ? null : e.getGroup().getId(),
                e.getPaidBy() == null ? null : e.getPaidBy().getId(),
                splitIds,
                e.getCreatedAt()
        );
    }
}
