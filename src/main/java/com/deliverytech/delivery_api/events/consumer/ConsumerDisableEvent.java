package com.deliverytech.delivery_api.events.consumer;

import com.deliverytech.delivery_api.model.Consumer;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ConsumerDisableEvent extends ApplicationEvent {

    private final Consumer consumer;
    private final String user;

    public ConsumerDisableEvent(Object source, Consumer consumer, String user) {
        super(source);
        this.consumer = consumer;
        this.user = user;
    }
}
