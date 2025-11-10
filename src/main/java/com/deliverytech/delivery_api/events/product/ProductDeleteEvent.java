package com.deliverytech.delivery_api.events.product;

import com.deliverytech.delivery_api.model.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductDeleteEvent extends ApplicationEvent {

    private final Product product;
    private final String user;

    public ProductDeleteEvent(Object source, Product product, String user) {
        super(source);
        this.product = product;
        this.user = user;
    }
}
