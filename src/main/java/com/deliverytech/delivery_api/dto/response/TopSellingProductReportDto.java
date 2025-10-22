package com.deliverytech.delivery_api.dto.response;

public record TopSellingProductReportDto(
        String productName,
        Long totalSold
) {
}
