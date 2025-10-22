package com.deliverytech.delivery_api.repository.projection;

public interface TopSellingProductProjection {
    String getProductName();
    Long getTotalSold();
}
