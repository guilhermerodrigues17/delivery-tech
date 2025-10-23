package com.deliverytech.delivery_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO de resposta para um item do pedido")
public record OrderItemResponseDto(
        UUID id,
        UUID productId,
        String productName,
        Integer quantity,
        BigDecimal subtotal) {

}
