package com.deliverytech.delivery_api.model.enums;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PENDING, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELED;

    private static final Map<OrderStatus, Set<OrderStatus>> allowedTransitions =
            new EnumMap<>(OrderStatus.class);

    static {
        allowedTransitions.put(PENDING, Set.of(CONFIRMED, CANCELED, PREPARING));
        allowedTransitions.put(CONFIRMED, Set.of(PREPARING, CANCELED));
        allowedTransitions.put(PREPARING, Set.of(OUT_FOR_DELIVERY, CANCELED));
        allowedTransitions.put(OUT_FOR_DELIVERY, Set.of(DELIVERED));

        allowedTransitions.put(DELIVERED, Set.of());
        allowedTransitions.put(CANCELED, Set.of());
    }

    public boolean canTransition(OrderStatus newStatus) {
        return allowedTransitions.get(this).contains(newStatus);
    }
}
