package com.splito.dto.response;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorResponse(
        String message,
        Map<String, String> errors,
        OffsetDateTime timestamp
) {}
