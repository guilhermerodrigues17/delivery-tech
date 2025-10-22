package com.deliverytech.delivery_api.dto.response;

import com.deliverytech.delivery_api.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderByPeriodReportDto(
        LocalDate date,
        Long totalOrders,
        BigDecimal totalSales,
        OrderStatus status
) {
}
