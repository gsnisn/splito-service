package com.splito.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class DebtService {

    private static final BigDecimal EPS = new BigDecimal("0.01");

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Transaction {
        private Long fromUserId;     // debtor (payer)
        private Long toUserId;       // creditor (receiver)
        private BigDecimal amount;   // scale 2
    }

    public List<Transaction> simplifyDebts(Map<Long, BigDecimal> balances) {

        // If null or empty => no transactions
        if (balances == null || balances.isEmpty()) {
            return Collections.emptyList();
        }

        // ---------- Build priority queues ----------
        // debtors: most negative first (e.g. -500 before -100)
        PriorityQueue<Map.Entry<Long, BigDecimal>> debtors =
                new PriorityQueue<>(Comparator.comparing(Map.Entry::getValue));

        // creditors: most positive first (e.g. 500 before 100)
        PriorityQueue<Map.Entry<Long, BigDecimal>> creditors =
                new PriorityQueue<>((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<Long, BigDecimal> e : balances.entrySet()) {
            Long userId = e.getKey();
            BigDecimal bal = safe2(e.getValue());

            if (bal.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new AbstractMap.SimpleEntry<>(userId, bal));
            } else if (bal.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new AbstractMap.SimpleEntry<>(userId, bal));
            }
        }

        // ---------- Greedy settlement ----------
        List<Transaction> result = new ArrayList<>();

        while (!debtors.isEmpty() && !creditors.isEmpty()) {

            Map.Entry<Long, BigDecimal> debtor = debtors.poll();     // negative
            Map.Entry<Long, BigDecimal> creditor = creditors.poll(); // positive

            BigDecimal debtorOwes = debtor.getValue().abs();         // positive
            BigDecimal creditorIsOwed = creditor.getValue();         // positive

            // Transfer as much as possible without overpaying either side
            BigDecimal amount = debtorOwes.min(creditorIsOwed).setScale(2, RoundingMode.HALF_UP);

            // Safety: skip zero transfers (can happen due to rounding)
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            result.add(new Transaction(
                    debtor.getKey(),
                    creditor.getKey(),
                    amount
            ));

            // Update balances after transfer
            BigDecimal newDebtorBal = safe2(debtor.getValue().add(amount));        // closer to 0
            BigDecimal newCreditorBal = safe2(creditor.getValue().subtract(amount)); // closer to 0

            // If still not settled, push back into queue
            if (newDebtorBal.abs().compareTo(EPS) > 0) {
                debtors.add(new AbstractMap.SimpleEntry<>(debtor.getKey(), newDebtorBal));
            }
            if (newCreditorBal.abs().compareTo(EPS) > 0) {
                creditors.add(new AbstractMap.SimpleEntry<>(creditor.getKey(), newCreditorBal));
            }
        }

        return result;
    }

    private static BigDecimal safe2(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
