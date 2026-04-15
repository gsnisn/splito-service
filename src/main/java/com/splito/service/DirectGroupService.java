package com.splito.service;

import com.splito.exception.ResourceNotFoundException;
import com.splito.model.DirectGroupPair;
import com.splito.model.SplitoGroup;
import com.splito.model.SplitoUser;
import com.splito.repository.DirectGroupPairRepository;
import com.splito.repository.GroupRepository;
import com.splito.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectGroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final DirectGroupPairRepository pairRepository;

    @Transactional
    public SplitoGroup getOrCreateDirectGroup(Long userAId, Long userBId) {

        if (userAId == null || userBId == null) {
            throw new IllegalArgumentException("userAId and userBId are required");
        }
        if (userAId.equals(userBId)) {
            throw new IllegalArgumentException("Users must be different");
        }

        long low = Math.min(userAId, userBId);
        long high = Math.max(userAId, userBId);

        return pairRepository.findByUserLowAndUserHigh(low, high)
                .map(p -> groupRepository.findById(p.getGroupId())
                        .orElseThrow(() -> new ResourceNotFoundException("Direct group not found")))
                .orElseGet(() -> createDirectGroup(low, high));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "groupList", allEntries = true)
    })
    protected SplitoGroup createDirectGroup(long low, long high) {
        SplitoUser u1 = userRepository.findById(low)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + low));
        SplitoUser u2 = userRepository.findById(high)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + high));

        SplitoGroup g = new SplitoGroup();
        g.setName("Direct: " + low + "-" + high);
        g.setDirect(true);
        g.setMembers(new ArrayList<>(List.of(u1, u2)));

        g = groupRepository.save(g);

        pairRepository.save(new DirectGroupPair(g.getId(), low, high));

        return g;
    }
}