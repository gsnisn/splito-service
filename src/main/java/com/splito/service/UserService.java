package com.splito.service;

import com.splito.dto.request.CreateUserRequest;
import com.splito.dto.request.UpdateUserRequest;
import com.splito.exception.ResourceNotFoundException;
import com.splito.exception.UnauthorizedException;
import com.splito.model.Expense;
import com.splito.model.Settlement;
import com.splito.model.SplitoGroup;
import com.splito.model.SplitoUser;
import com.splito.repository.ExpenseRepository;
import com.splito.repository.GroupRepository;
import com.splito.repository.SettlementRepository;
import com.splito.repository.UserRepository;
import com.splito.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final BigDecimal EPS = new BigDecimal("0.01");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final BalanceService balanceService;

    @Transactional
    @Caching(
            put = {
                    @CachePut(cacheNames = "users", key = "#result.id")
            },
            evict = {
                    @CacheEvict(cacheNames = "userList", allEntries = true)
            }
    )
    public SplitoUser create(CreateUserRequest req) {
        SplitoUser u = new SplitoUser();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setPassword(req.getPassword());

        try {
            return userRepository.save(u);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("email/phone already exists");
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "userList")
    public List<SplitoUser> list() {
        logger.info("userList");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "users", key = "#id")
    public SplitoUser get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    @Caching(
            put = {
                    @CachePut(cacheNames = "users", key = "#result.id")
            },
            evict = {
                    @CacheEvict(cacheNames = "userList", allEntries = true)
            }
    )
    public SplitoUser update(Long userId, UpdateUserRequest req) {

        Long authUserId = getAuthenticatedUserId();

        if (!authUserId.equals(userId)) {
            throw new UnauthorizedException("You can only update your own profile");
        }

        SplitoUser existing = get(userId);
        existing.setName(req.getName());
        existing.setPhone(req.getPhone());

        return userRepository.save(existing);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "userList", allEntries = true),
            @CacheEvict(cacheNames = "users", key = "#userId"),
            @CacheEvict(cacheNames = "groupList", allEntries = true),
            @CacheEvict(cacheNames = "groups", allEntries = true),
            @CacheEvict(cacheNames = "groupBalances", allEntries = true)
    })
    public void delete(Long userId) {

        Long authUserId = getAuthenticatedUserId();

        if (!authUserId.equals(userId)) {
            throw new UnauthorizedException("You can only delete your own account");
        }

        get(userId);

        List<SplitoGroup> groups = groupRepository.findByMembersId(userId);

        for (SplitoGroup g : groups) {
            List<Expense> expenses = expenseRepository.findByGroupId(g.getId());
            List<Settlement> settlements = settlementRepository.findByGroupId(g.getId());

            Map<Long, BigDecimal> balances = balanceService.calculateGroupBalances(expenses, settlements);
            BigDecimal bal = balances.getOrDefault(userId, BigDecimal.ZERO);

            if (bal.abs().compareTo(EPS) > 0) {
                throw new IllegalStateException("Cannot delete user: pending balance in groupId=" + g.getId());
            }
        }

        for (SplitoGroup g : groups) {
            g.getMembers().removeIf(u -> Objects.equals(u.getId(), userId));
            groupRepository.save(g);
        }

        userRepository.deleteById(userId);
    }

    private SplitoUser getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new UnauthorizedException("Unauthenticated");
        }

        return get(cud.getId());
    }

    @Transactional(readOnly = true)
    public SplitoUser me() {
        return getAuthenticatedUser();
    }

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "userList", allEntries = true),
            },
            put = {
                @CachePut(cacheNames = "users", key = "#result.id", condition = "#result != null")
            }
    )
    public SplitoUser updateMe(UpdateUserRequest req) {
        SplitoUser user = getAuthenticatedUser();
        user.setName(req.getName());
        user.setPhone(req.getPhone());
        return userRepository.save(user);
    }

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new UnauthorizedException("Unauthenticated");
        }

        return cud.getId();
    }
}