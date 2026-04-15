package com.splito.dto.mapper;

import com.splito.dto.response.BalanceResponse;
import com.splito.dto.response.GroupBalanceResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class BalanceMapper {

    public List<BalanceResponse> toBalanceResponses(Map<Long, BigDecimal> balancesByUserId) {
        if (balancesByUserId == null) return List.of();

        return balancesByUserId.entrySet().stream()
                .map(e -> new BalanceResponse(e.getKey(), e.getValue()))
                .toList();
    }

    public GroupBalanceResponse toGroupBalanceResponse(Long groupId, Map<Long, BigDecimal> balancesByUserId) {
        return new GroupBalanceResponse(groupId, toBalanceResponses(balancesByUserId));
    }
}
