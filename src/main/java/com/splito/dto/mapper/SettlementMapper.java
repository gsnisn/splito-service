package com.splito.dto.mapper;

import com.splito.dto.response.SettlementResponse;
import com.splito.model.Settlement;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {

    public SettlementResponse toResponse(Settlement s) {
        if (s == null) return null;

        return new SettlementResponse(
                s.getId(),
                s.getFromUser() == null ? null : s.getFromUser().getId(),
                s.getToUser() == null ? null : s.getToUser().getId(),
                s.getAmount(),
                s.getCreatedAt()
        );
    }
}
