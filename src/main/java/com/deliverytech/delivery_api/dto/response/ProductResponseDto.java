package com.deliverytech.delivery_api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

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
