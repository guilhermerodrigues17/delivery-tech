package com.deliverytech.delivery_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO de resposta para um produto")
public record ProductResponseDto(
        UUID id,
        String name,
        String description,
        String category,
        Boolean available,
        BigDecimal price,
        String restaurantName,
        UUID restaurantId) {

}
