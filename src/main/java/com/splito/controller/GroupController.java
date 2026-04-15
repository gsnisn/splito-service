package com.splito.controller;

import com.splito.dto.mapper.GroupMapper;
import com.splito.dto.request.CreateGroupRequest;
import com.splito.dto.request.UpdateGroupMembersRequest;
import com.splito.dto.request.UpdateGroupNameRequest;
import com.splito.dto.response.GroupResponse;
import com.splito.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupMapper groupMapper;

    @PostMapping
    public GroupResponse create(@Valid @RequestBody CreateGroupRequest req) {
        return groupMapper.toResponse(groupService.create(req));
    }

    @GetMapping
    public List<GroupResponse> list() {
        return groupService.list().stream().map(groupMapper::toResponse).toList();
    }

    @GetMapping("/{groupId}")
    public GroupResponse get(@PathVariable Long groupId) {
        return groupMapper.toResponse(groupService.get(groupId));
    }

    @PutMapping("/{groupId}")
    public GroupResponse updateName(@PathVariable Long groupId, @Valid @RequestBody UpdateGroupNameRequest req) {
        return groupMapper.toResponse(groupService.updateName(groupId, req));
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long groupId) {
        groupService.delete(groupId);
    }

    @PostMapping("/{groupId}/members")
    public GroupResponse addMembers(@PathVariable Long groupId, @Valid @RequestBody UpdateGroupMembersRequest req) {
        return groupMapper.toResponse(groupService.addMembers(groupId, req));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public GroupResponse removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        return groupMapper.toResponse(groupService.removeMember(groupId, userId));
    }

    @GetMapping("/{groupId}/balances")
    public Map<Long, BigDecimal> balances(@PathVariable Long groupId) {
        return groupService.balances(groupId);
    }
}
