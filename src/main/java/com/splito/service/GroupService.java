package com.splito.service;

import com.splito.dto.request.CreateGroupRequest;
import com.splito.dto.request.UpdateGroupMembersRequest;
import com.splito.dto.request.UpdateGroupNameRequest;
import com.splito.event.GroupCreatedEvent;
import com.splito.exception.ResourceNotFoundException;
import com.splito.kafka.NotificationEventProducer;
import com.splito.model.Expense;
import com.splito.model.Settlement;
import com.splito.model.SplitoGroup;
import com.splito.model.SplitoUser;
import com.splito.repository.ExpenseRepository;
import com.splito.repository.GroupRepository;
import com.splito.repository.SettlementRepository;
import com.splito.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final BigDecimal EPS = new BigDecimal("0.01");

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final BalanceService balanceService;
    private final NotificationEventProducer notificationEventProducer;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "groupList", allEntries = true)
    })
    public SplitoGroup create(CreateGroupRequest req) {
        List<Long> distinctIds = req.getMemberIds().stream().filter(Objects::nonNull).distinct().toList();
        List<SplitoUser> members = userRepository.findAllById(distinctIds);

        if (members.size() != distinctIds.size()) {
            throw new ResourceNotFoundException("One or more users not found");
        }

        SplitoGroup g = new SplitoGroup();
        g.setName(req.getName());
        g.setMembers(members);
        g.setDirect(false);

        g = groupRepository.save(g);

        notificationEventProducer.publishGroupCreated(
                new GroupCreatedEvent(
                        g.getId(),
                        g.getName(),
                        g.getMembers().stream().map(SplitoUser::getId).toList(),
                        g.isDirect(),
                        Instant.now()
                )
        );

        return g;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "groupList")
    public List<SplitoGroup> list() {
        return groupRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "groups", key = "#groupId")
    public SplitoGroup get(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "groups", key = "#groupId"),
            @CacheEvict(cacheNames = "groupList", allEntries = true),
            @CacheEvict(cacheNames = "groupBalances", key = "#groupId")
    })
    public SplitoGroup updateName(Long groupId, UpdateGroupNameRequest req) {
        SplitoGroup g = get(groupId);
        g.setName(req.getName());
        return groupRepository.save(g);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "groups", key = "#groupId"),
            @CacheEvict(cacheNames = "groupList", allEntries = true),
            @CacheEvict(cacheNames = "groupBalances", key = "#groupId")
    })
    public void delete(Long groupId) {
        SplitoGroup g = get(groupId);

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);

        Map<Long, BigDecimal> balances = balanceService.calculateGroupBalances(expenses, settlements);
        boolean settled = balances.values().stream().allMatch(b -> b == null || b.abs().compareTo(EPS) <= 0);

        if (!settled) {
            if (g.isDirect()) {
                throw new IllegalStateException("Cannot delete direct group: pending balances exist. Settle up first.");
            }
            throw new IllegalStateException("Cannot delete group: pending balances exist. Settle up first.");
        }

        groupRepository.delete(g);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "groups", key = "#groupId"),
            @CacheEvict(cacheNames = "groupList", allEntries = true),
            @CacheEvict(cacheNames = "groupBalances", key = "#groupId")
    })
    public SplitoGroup addMembers(Long groupId, UpdateGroupMembersRequest req) {
        SplitoGroup group = get(groupId);

        List<Long> distinctIds = req.getMemberIds().stream().filter(Objects::nonNull).distinct().toList();
        List<SplitoUser> users = userRepository.findAllById(distinctIds);

        if (users.size() != distinctIds.size()) {
            throw new ResourceNotFoundException("One or more users not found");
        }

        Set<Long> existingIds = group.getMembers().stream().map(SplitoUser::getId).collect(Collectors.toSet());
        for (SplitoUser u : users) {
            if (!existingIds.contains(u.getId())) {
                group.getMembers().add(u);
            }
        }
        return groupRepository.save(group);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "groups", key = "#groupId"),
            @CacheEvict(cacheNames = "groupList", allEntries = true),
            @CacheEvict(cacheNames = "groupBalances", key = "#groupId")
    })
    public SplitoGroup removeMember(Long groupId, Long userId) {
        SplitoGroup group = get(groupId);

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
        Map<Long, BigDecimal> balances = balanceService.calculateGroupBalances(expenses, settlements);
        BigDecimal bal = balances.getOrDefault(userId, BigDecimal.ZERO);
        if (bal.abs().compareTo(EPS) > 0) {
            throw new IllegalStateException("Cannot remove member: pending balance exists. Settle up first.");
        }

        boolean removed = group.getMembers().removeIf(u -> Objects.equals(u.getId(), userId));
        if (!removed) {
            throw new ResourceNotFoundException("User is not a member of this group");
        }

        return groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "groupBalances", key = "#groupId")
    public Map<Long, BigDecimal> balances(Long groupId) {
        get(groupId);

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);

        return balanceService.calculateGroupBalances(expenses, settlements);
    }
}