package com.deliverytech.delivery_api.events.restaurant;

import com.deliverytech.delivery_api.model.Restaurant;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RestaurantUpdateEvent extends ApplicationEvent {
    private final Restaurant restaurant;
    private final String user;

    public RestaurantUpdateEvent(Object source, Restaurant restaurant, String user) {
        super(source);
        this.restaurant = restaurant;
        this.user = user;
    }
}
