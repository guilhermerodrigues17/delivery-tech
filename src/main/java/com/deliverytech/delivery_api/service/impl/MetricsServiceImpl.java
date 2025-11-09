package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.model.Order;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricsServiceImpl {

    private final MeterRegistry meterRegistry;

    public void incrementOrdersProcessed(Order order) {
        List<Tag> tags = List.of(
                Tag.of("status", order.getStatus().toString()),
                Tag.of("restaurant_name", order.getRestaurant().getName())
        );

        meterRegistry.counter("delivery_api.orders.processed.total", tags).increment();
    }

    public void incrementOrdersDelivered(Order order) {
        if (order.getStatus() != OrderStatus.DELIVERED) {
            return;
        }

        List<Tag> tags = List.of(
                Tag.of("restaurant_name", order.getRestaurant().getName())
        );

        meterRegistry.counter("delivery_api.orders.delivered.total", tags).increment();
    }

    public void incrementOrdersCanceled(Order order) {
        if (order.getStatus() != OrderStatus.CANCELED) {
            return;
        }

        List<Tag> tags = List.of(
                Tag.of("restaurant_name", order.getRestaurant().getName())
        );

        meterRegistry.counter("delivery_api.orders.canceled.total", tags).increment();
    }
}
