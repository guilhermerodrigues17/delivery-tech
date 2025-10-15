package com.deliverytech.delivery_api.dto.response;

import java.util.UUID;

public record RestaurantResponseDto(
        UUID id,
        String name,
        String category,
        String phoneNumber,
        String address,
        Boolean active,
        String deliveryTax) {

}
