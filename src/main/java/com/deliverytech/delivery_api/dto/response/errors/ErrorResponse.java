package com.deliverytech.delivery_api.dto.response.errors;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ErrorResponse {

    private final boolean success = false;
    private final ErrorDetails error;
    private final Instant timestamp;

    private ErrorResponse(String code, String message, String details) {
        this.error = new ErrorDetails(code, message, details);
        this.timestamp = Instant.now();
    }

    public static ErrorResponse of(String code, String message, String details) {
        return new ErrorResponse(code, message, details);
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, message);
    }
}
