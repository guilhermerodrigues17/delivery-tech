package com.deliverytech.delivery_api.validation;

import com.deliverytech.delivery_api.exceptions.BusinessException;
import com.deliverytech.delivery_api.model.Product;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderValidator {

    private final Tracer tracer;

    @NewSpan("checkProductBelongsRestaurant")
    public void validateProductBelongsRestaurant(Restaurant restaurant, Product product) {
        tracer.currentSpan().tag("restaurant.name", restaurant.getName());
        tracer.currentSpan().tag("product.name", product.getName());

        if (!product.getRestaurant().getId().equals(restaurant.getId())) {
            tracer.currentSpan().tag("validation.result", "failed");
            throw new BusinessException(
                    String.format("O produto '%s' (%s) não pertence ao restaurante informado.",
                            product.getName(), product.getId()));
        }

        tracer.currentSpan().tag("validation.result", "success");
    }

    @NewSpan("checkStatusTransition")
    public void validateStatusTransition(OrderStatus current, OrderStatus newStatus) {
        //
    }
}
