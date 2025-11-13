package com.deliverytech.delivery_api.validation;

import com.deliverytech.delivery_api.exceptions.ConflictException;
import com.deliverytech.delivery_api.repository.RestaurantRepository;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantValidator {

    private final RestaurantRepository restaurantRepository;
    private final Tracer tracer;

    @NewSpan("checkRestaurantNameConflict")
    public void validateName(String name) {
        tracer.currentSpan().tag("restaurant.name", name);

        if (restaurantRepository.existsByName(name)) {
            tracer.currentSpan().tag("validation.result", "failed");
            throw new ConflictException("Nome de restaurante já está em uso");
        }

        tracer.currentSpan().tag("validation.result", "success");
    }
}