package com.deliverytech.delivery_api.dto.response;

import com.deliverytech.delivery_api.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
        UUID id,
        String consumerName,
        String restaurantName,
        String deliveryAddress,
        OrderStatus status,
        LocalDateTime orderDate,
        LocalDateTime lastModifiedDate,
        List<OrderItemResponseDto> items,
        BigDecimal subtotal,
        BigDecimal deliveryTax,
        BigDecimal total) {
}
