package com.splito.service;

import com.splito.model.Expense;
import com.splito.model.ExpenseSplit;
import com.splito.model.Settlement;
import com.splito.model.SplitoUser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BalanceService {

    /**
     * Returns net balances per user for a group:
     *  - Positive => user should receive money
     *  - Negative => user owes money
     *
     * Supports:
     *  - EQUAL split: expense.getSplitBetween()
     *  - UNEQUAL/EXACT split: expense.getSplits() (if present & not empty)
     */
    public Map<Long, BigDecimal> calculateGroupBalances(List<Expense> expenses, List<Settlement> settlements) {

        Map<Long, BigDecimal> balances = new HashMap<>();

        // ---- existing EXPENSE logic (your current code) ----
        for (Expense expense : expenses) {

            BigDecimal totalAmount = expense.getAmount();

            if (expense.getSplits() != null && !expense.getSplits().isEmpty()) {
                for (ExpenseSplit split : expense.getSplits()) {
                    Long userId = split.getUser().getId();
                    BigDecimal share = split.getAmount().setScale(2, RoundingMode.HALF_UP);

                    balances.put(userId, balances.getOrDefault(userId, BigDecimal.ZERO).subtract(share));
                }
            } else {
                if (expense.getSplitBetween() == null || expense.getSplitBetween().isEmpty()) continue;

                int count = expense.getSplitBetween().size();
                BigDecimal splitAmount = totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

                for (SplitoUser user : expense.getSplitBetween()) {
                    balances.put(user.getId(), balances.getOrDefault(user.getId(), BigDecimal.ZERO).subtract(splitAmount));
                }
            }

            Long paidById = expense.getPaidBy().getId();
            balances.put(paidById, balances.getOrDefault(paidById, BigDecimal.ZERO).add(totalAmount));
        }

        // ---- APPLY SETTLEMENTS (reduce pending debts) ----
        for (Settlement s : settlements) {
            Long fromId = s.getFromUser().getId();
            Long toId = s.getToUser().getId();
            BigDecimal amt = s.getAmount().setScale(2, RoundingMode.HALF_UP);

            // payer owes less => increase balance
            balances.put(fromId, balances.getOrDefault(fromId, BigDecimal.ZERO).add(amt));

            // receiver is owed less => decrease balance
            balances.put(toId, balances.getOrDefault(toId, BigDecimal.ZERO).subtract(amt));
        }

        return balances;
    }


}
