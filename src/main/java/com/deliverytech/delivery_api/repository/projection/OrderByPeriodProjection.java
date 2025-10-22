package com.deliverytech.delivery_api.repository.projection;

import com.deliverytech.delivery_api.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface OrderByPeriodProjection {
    LocalDate getDate();
    Long getTotalOrders();
    BigDecimal getTotalSales();
    OrderStatus getStatus();
}
