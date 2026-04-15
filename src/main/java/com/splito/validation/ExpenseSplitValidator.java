package com.splito.validation;

import com.splito.dto.request.CreateExpenseRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ExpenseSplitValidator
        implements ConstraintValidator<ValidExpenseSplit, CreateExpenseRequest> {

    @Override
    public boolean isValid(CreateExpenseRequest req, ConstraintValidatorContext ctx) {

        if (req == null) return true;

        List<?> exact = req.getExactSplits();
        List<?> equal = req.getSplitBetweenUserIds();

        boolean hasExact = exact != null && !exact.isEmpty();
        boolean hasEqual = equal != null && !equal.isEmpty();

        if (!hasExact && !hasEqual) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(
                    "Provide either exactSplits or splitBetweenUserIds"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
