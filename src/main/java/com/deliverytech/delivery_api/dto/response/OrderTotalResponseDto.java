package com.deliverytech.delivery_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO de resposta para o cálculo do valor total do pedido")
public record OrderTotalResponseDto(
        BigDecimal subtotal,
        BigDecimal deliveryTax,
        BigDecimal total
) {
}
