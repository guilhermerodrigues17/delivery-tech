package com.deliverytech.delivery_api.controller.dto;

import java.util.UUID;

public record ConsumerResponseDto(
        UUID id,
        String name,
        String email,
        String phoneNumber,
        String address,
        Boolean active
) {
}
