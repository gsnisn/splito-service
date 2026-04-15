package com.splito.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
