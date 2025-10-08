package com.deliverytech.delivery_api.dto.response;

import java.util.UUID;

public record ProductResponseDto(UUID id, String name, String description, String category,
        Boolean available, String restaurantName, UUID restaurantId) {

}
