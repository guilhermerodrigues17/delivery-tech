package com.deliverytech.delivery_api.config.converter;

import com.deliverytech.delivery_api.model.enums.OrderStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToOrderStatusConverter implements Converter<String, OrderStatus> {
    @Override
    public OrderStatus convert(String source) {
        if (source.isEmpty()) {
            return null;
        }

        try {
            return OrderStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Valor de status inválido: " + source, ex);
        }
    }
}
