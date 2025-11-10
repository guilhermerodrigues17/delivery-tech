package com.deliverytech.delivery_api.events.order;

import com.deliverytech.delivery_api.model.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCancelEvent extends ApplicationEvent {

    private final Order order;
    private final String user;

    public OrderCancelEvent(Object source, Order order, String user) {
        super(source);
        this.order = order;
        this.user = user;
    }
}
