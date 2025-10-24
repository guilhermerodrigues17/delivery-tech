package com.deliverytech.delivery_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO de resposta para um restaurante")
public record RestaurantResponseDto(
        UUID id,
        String name,
        String category,
        String phoneNumber,
        String address,
        Boolean active,
        String deliveryTax) {

}
