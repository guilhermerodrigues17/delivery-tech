package com.deliverytech.delivery_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de resposta para o relatório de clientes mais ativos")
public record ActiveConsumerReportDto(
        @Schema(description = "Nome do cliente")
        String consumerName,
        @Schema(description = "E-mail do cliente")
        String consumerEmail,
        @Schema(description = "Número total de pedidos feitos")
        Long totalOrders
) {
}
