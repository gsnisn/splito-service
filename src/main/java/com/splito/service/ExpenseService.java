package com.splito.service;

import com.splito.dto.request.CreateExpenseRequest;
import com.splito.dto.request.CreateExpenseRequest.SplitShare;
import com.splito.event.ExpenseCreatedEvent;
import com.splito.exception.ResourceNotFoundException;
import com.splito.kafka.NotificationEventProducer;
import com.splito.model.Expense;
import com.splito.model.ExpenseSplit;
import com.splito.model.SplitoGroup;
import com.splito.model.SplitoUser;
import com.splito.repository.ExpenseRepository;
import com.splito.repository.GroupRepository;
import com.splito.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final NotificationEventProducer notificationEventProducer;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "groupBalances", key = "#p0.groupId"),
            @CacheEvict(cacheNames = "groups", key = "#p0.groupId"),
            @CacheEvict(cacheNames = "groupList", allEntries = true)
    })
    public Expense addExpense(CreateExpenseRequest request) {
        Expense expense = new Expense();
        applyExpenseUpsert(expense, request);
        Expense savedExpense = expenseRepository.save(expense);

        ExpenseCreatedEvent event = new ExpenseCreatedEvent(
                savedExpense.getId(),
                savedExpense.getGroup().getId(),
                savedExpense.getPaidBy().getId(),
                savedExpense.getAmount(),
                savedExpense.getDescription(),
                savedExpense.getSplits()
                        .stream()
                        .map(split -> split.getUser().getId())
                        .toList(),
                Instant.now()
        );

        notificationEventProducer.publishExpenseCreated(event);

        return savedExpense;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "groupBalances", key = "#groupId"),
            @CacheEvict(cacheNames = "groups", key = "#groupId"),
            @CacheEvict(cacheNames = "groupList", allEntries = true)
    })
    public Expense updateExpense(Long groupId, Long expenseId, CreateExpenseRequest request) {
        Expense existing = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (existing.getGroup() == null || !Objects.equals(existing.getGroup().getId(), groupId)) {
            throw new ResourceNotFoundException("Expense not found in this group");
        }

        request.setGroupId(groupId);
        applyExpenseUpsert(existing, request);

        return expenseRepository.save(existing);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "groupBalances", key = "#groupId"),
            @CacheEvict(cacheNames = "groups", key = "#groupId"),
            @CacheEvict(cacheNames = "groupList", allEntries = true)
    })
    public void deleteExpense(Long groupId, Long expenseId) {
        Expense e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (e.getGroup() == null || !Objects.equals(e.getGroup().getId(), groupId)) {
            throw new ResourceNotFoundException("Expense not found in this group");
        }

        expenseRepository.delete(e);
    }

    private void applyExpenseUpsert(Expense expense, CreateExpenseRequest request) {

        if (request.getGroupId() == null) throw new IllegalArgumentException("groupId is required");
        if (request.getPaidByUserId() == null) throw new IllegalArgumentException("paidByUserId is required");
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }

        BigDecimal totalAmount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        boolean hasExact = request.getExactSplits() != null && !request.getExactSplits().isEmpty();

        SplitoGroup group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        Set<Long> groupMemberIds = group.getMembers().stream()
                .map(SplitoUser::getId)
                .collect(Collectors.toSet());

        SplitoUser paidBy = userRepository.findById(request.getPaidByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Paid-by user not found"));

        if (!groupMemberIds.contains(paidBy.getId())) {
            throw new IllegalArgumentException("paidByUserId is not a member of groupId=" + group.getId());
        }

        List<Long> participantIds = new ArrayList<>();

        if (hasExact) {
            for (SplitShare s : request.getExactSplits()) {
                Long userId = s.getUserId();
                if (userId == null) throw new IllegalArgumentException("exactSplits.userId is required");
                if (!groupMemberIds.contains(userId)) {
                    throw new IllegalArgumentException("User " + userId + " is not a member of groupId=" + group.getId());
                }
                participantIds.add(userId);
            }
        } else {
            if (request.getSplitBetweenUserIds() == null || request.getSplitBetweenUserIds().isEmpty()) {
                throw new IllegalArgumentException("splitBetweenUserIds is required for equal split");
            }
            for (Long userId : request.getSplitBetweenUserIds()) {
                if (userId == null) throw new IllegalArgumentException("splitBetweenUserIds cannot contain null");
                if (!groupMemberIds.contains(userId)) {
                    throw new IllegalArgumentException("User " + userId + " is not a member of groupId=" + group.getId());
                }
                participantIds.add(userId);
            }
        }

        participantIds = participantIds.stream().distinct().toList();

        List<SplitoUser> participants = userRepository.findAllById(participantIds);
        if (participants.size() != participantIds.size()) {
            throw new ResourceNotFoundException("One or more split participants not found");
        }

        expense.setDescription(request.getDescription());
        expense.setAmount(totalAmount);
        expense.setPaidBy(paidBy);
        expense.setGroup(group);
        expense.setSplitBetween(participants);

        if (expense.getSplits() == null) {
            expense.setSplits(new ArrayList<>());
        }
        expense.getSplits().clear();

        if (hasExact) {
            BigDecimal sum = BigDecimal.ZERO;
            Set<Long> seen = new HashSet<>();

            for (SplitShare s : request.getExactSplits()) {
                if (s.getAmount() == null) throw new IllegalArgumentException("exactSplits.amount is required");
                if (s.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("exactSplits.amount must be >= 0");
                }
                if (!seen.add(s.getUserId())) {
                    throw new IllegalArgumentException("Duplicate userId in exactSplits: " + s.getUserId());
                }
                sum = sum.add(s.getAmount());
            }

            sum = sum.setScale(2, RoundingMode.HALF_UP);
            if (sum.compareTo(totalAmount) != 0) {
                throw new IllegalArgumentException(
                        "Sum of exactSplits (" + sum + ") must equal total amount (" + totalAmount + ")"
                );
            }

            for (SplitShare s : request.getExactSplits()) {
                SplitoUser u = userRepository.findById(s.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("Split user not found: " + s.getUserId()));

                ExpenseSplit es = new ExpenseSplit();
                es.setExpense(expense);
                es.setUser(u);
                es.setAmount(s.getAmount().setScale(2, RoundingMode.HALF_UP));

                expense.getSplits().add(es);
            }
        }
    }
}