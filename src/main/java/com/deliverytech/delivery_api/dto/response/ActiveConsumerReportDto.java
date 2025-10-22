package com.deliverytech.delivery_api.dto.response;

public record ActiveConsumerReportDto(
        String consumerName,
        String consumerEmail,
        Long totalOrders
) {
}
