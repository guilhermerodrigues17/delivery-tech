package com.deliverytech.delivery_api.dto.response.errors;

public record ErrorDetails(
        String code,
        String message,
        String details
) {
}
