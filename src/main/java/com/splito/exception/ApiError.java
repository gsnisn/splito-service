package com.splito.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Standard API error response")
public class ApiError {

    @Schema(description = "Timestamp when the error occurred", example = "2026-01-12T14:03:11.291")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP status description", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "User not found")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/v1/splito/users/99")
    private String path;

    public ApiError(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
}

