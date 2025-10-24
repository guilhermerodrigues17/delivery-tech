package com.deliverytech.delivery_api.dto.response;

import com.deliverytech.delivery_api.model.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO de resposta para o relatório de pedidos por período")
public record OrderByPeriodReportDto(
        @Schema(description = "Data em que os pedidos foram feitos")
        LocalDate date,
        @Schema(description = "Número total de pedidos")
        Long totalOrders,
        @Schema(description = "Valor total em vendas")
        BigDecimal totalSales,
        @Schema(description = "Status dos pedidos agrupados")
        OrderStatus status
) {
}
