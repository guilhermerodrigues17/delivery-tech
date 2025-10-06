package com.deliverytech.delivery_api.dto.request;

public record ConsumerRequestDto(
        String name,
        String email,
        String phoneNumber,
        String address) {
}
