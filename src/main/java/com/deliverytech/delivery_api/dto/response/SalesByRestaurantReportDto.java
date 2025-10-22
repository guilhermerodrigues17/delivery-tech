package com.deliverytech.delivery_api.dto.response;

import java.math.BigDecimal;

public record SalesByRestaurantReportDto(
        String restaurantName,
        BigDecimal totalSales
) {
}
