package com.splito.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExpenseSplitValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExpenseSplit {
    String message() default "Either exactSplits or splitBetweenUserIds must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
