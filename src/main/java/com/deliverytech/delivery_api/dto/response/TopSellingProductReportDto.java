package com.deliverytech.delivery_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de resposta para o relatório de produtos mais vendidos")
public record TopSellingProductReportDto(
        @Schema(description = "Nome do produto")
        String productName,
        @Schema(description = "Quantidade total vendida")
        Long totalSold
) {
}
