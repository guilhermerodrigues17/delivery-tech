package com.deliverytech.delivery_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO de resposta para o relatório de vendas por restaurante")
public record SalesByRestaurantReportDto(
        @Schema(description = "Nome do restaurante")
        String restaurantName,
        @Schema(description = "Valor total em vendas")
        BigDecimal totalSales
) {
}
