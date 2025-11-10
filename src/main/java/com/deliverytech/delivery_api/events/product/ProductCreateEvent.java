package com.deliverytech.delivery_api.events.product;

import com.deliverytech.delivery_api.model.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductCreateEvent extends ApplicationEvent {

    private final Product product;
    private final String user;

    public ProductCreateEvent(Object source, Product product, String user) {
        super(source);
        this.product = product;
        this.user = user;
    }
}
