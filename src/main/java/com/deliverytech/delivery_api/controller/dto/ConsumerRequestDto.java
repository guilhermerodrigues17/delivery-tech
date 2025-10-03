package com.deliverytech.delivery_api.controller.dto;

public record ConsumerRequestDto(
        String name,
        String email,
        String phoneNumber,
        String address
) {
}
