package com.deliverytech.delivery_api.dto.response;

import com.deliverytech.delivery_api.model.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO de resposta para o sumário dos pedidos")
public record OrderSummaryResponseDto(
        UUID id,
        String restaurantName,
        OrderStatus status,
        BigDecimal total
) {
}
