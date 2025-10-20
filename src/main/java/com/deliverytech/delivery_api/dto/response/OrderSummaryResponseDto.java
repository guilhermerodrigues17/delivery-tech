package com.deliverytech.delivery_api.dto.response;

import com.deliverytech.delivery_api.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderSummaryResponseDto(
        UUID id,
        String restaurantName,
        OrderStatus status,
        BigDecimal total
) {
}
