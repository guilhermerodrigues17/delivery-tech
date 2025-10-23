package com.deliverytech.delivery_api.dto.response;

import com.deliverytech.delivery_api.model.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "DTO de resposta para um pedido")
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
