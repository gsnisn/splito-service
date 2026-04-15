package com.splito.dto.mapper;

import com.splito.dto.response.GroupResponse;
import com.splito.model.SplitoGroup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupMapper {

    private final UserMapper userMapper;

    public GroupMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public GroupResponse toResponse(SplitoGroup g) {
        if (g == null) return null;

        List<?> members = g.getMembers() == null ? List.of() : g.getMembers();
        return new GroupResponse(
                g.getId(),
                g.getName(),
                g.getMembers() == null ? List.of() : g.getMembers().stream().map(userMapper::toResponse).toList()
        );
    }
}
