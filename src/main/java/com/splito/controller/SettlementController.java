package com.splito.controller;

import com.splito.dto.mapper.SettlementMapper;
import com.splito.dto.request.CreateSettlementRequest;
import com.splito.dto.response.SettlementResponse;
import com.splito.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    private final SettlementMapper settlementMapper;

    @PostMapping
    public SettlementResponse create(@PathVariable Long groupId, @Valid @RequestBody CreateSettlementRequest req) {
        return settlementMapper.toResponse(settlementService.create(groupId, req));
    }

    @GetMapping
    public List<SettlementResponse> list(@PathVariable Long groupId) {
        return settlementService.list(groupId).stream().map(settlementMapper::toResponse).toList();
    }
}
