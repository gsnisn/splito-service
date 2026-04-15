package com.splito.dto.mapper;

import com.splito.dto.response.UserResponse;
import com.splito.model.SplitoUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(SplitoUser u) {
        if (u == null) return null;
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone());
    }
}
