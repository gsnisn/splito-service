package com.splito.dto.response;

import java.util.List;

public record GroupResponse(
        Long id,
        String name,
        List<UserResponse> members
) {}
