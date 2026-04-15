package com.splito.service;

import com.splito.dto.request.CreateSettlementRequest;
import com.splito.exception.ResourceNotFoundException;
import com.splito.model.Settlement;
import com.splito.model.SplitoGroup;
import com.splito.model.SplitoUser;
import com.splito.repository.GroupRepository;
import com.splito.repository.SettlementRepository;
import com.splito.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SettlementRepository settlementRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "groupBalances", key = "#groupId"),
            @CacheEvict(cacheNames = "groups", key = "#groupId"),
            @CacheEvict(cacheNames = "groupList", allEntries = true)
    })
    public Settlement create(Long groupId, CreateSettlementRequest req) {

        if (req.getFromUserId().equals(req.getToUserId())) {
            throw new IllegalArgumentException("fromUserId and toUserId cannot be same");
        }

        BigDecimal amount = req.getAmount().setScale(2, RoundingMode.HALF_UP);

        SplitoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        Set<Long> memberIds = group.getMembers().stream().map(SplitoUser::getId).collect(Collectors.toSet());
        if (!memberIds.contains(req.getFromUserId()) || !memberIds.contains(req.getToUserId())) {
            throw new IllegalArgumentException("Both users must be members of this group");
        }

        SplitoUser from = userRepository.findById(req.getFromUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.getFromUserId()));
        SplitoUser to = userRepository.findById(req.getToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.getToUserId()));

        Settlement s = new Settlement();
        s.setGroup(group);
        s.setFromUser(from);
        s.setToUser(to);
        s.setAmount(amount);

        return settlementRepository.save(s);
    }

    @Transactional(readOnly = true)
    public List<Settlement> list(Long groupId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        return settlementRepository.findByGroupId(groupId);
    }
}