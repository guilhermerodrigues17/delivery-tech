package com.deliverytech.delivery_api.repository.projection;

public interface ActiveConsumerProjection {
    String getConsumerName();
    String getConsumerEmail();
    Long getTotalOrders();
}
