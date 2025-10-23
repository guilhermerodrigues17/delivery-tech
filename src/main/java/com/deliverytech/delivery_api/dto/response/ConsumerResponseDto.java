package com.deliverytech.delivery_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO de resposta para os dados de um cliente")
public record ConsumerResponseDto(
        UUID id,
        String name,
        String email,
        String phoneNumber,
        String address,
        Boolean active) {
}
