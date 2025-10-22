package com.deliverytech.delivery_api.repository.projection;

import java.math.BigDecimal;

public interface SalesByRestaurantProjection {
    String getRestaurantName();
    BigDecimal getTotalSales();
}
