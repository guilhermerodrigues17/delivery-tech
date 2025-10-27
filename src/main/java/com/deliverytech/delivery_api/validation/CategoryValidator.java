package com.deliverytech.delivery_api.validation;

import com.deliverytech.delivery_api.validation.annotations.ValidCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class CategoryValidator implements ConstraintValidator<ValidCategory, String> {
    private final List<String> VALID_CATEGORIES = List.of("BRASILEIRA", "ITALIANA", "JAPONESA", "CHINESA", "MEXICANA",
            "FAST_FOOD", "PIZZA", "HAMBURGUER", "SAUDAVEL", "VEGETARIANA", "VEGANA", "SOBREMESAS", "BEBIDAS");

    @Override
    public void initialize(ValidCategory constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String category, ConstraintValidatorContext context) {
        if (category == null) {
            return true;
        }

        var trimmedCategory = category.trim();
        if (trimmedCategory.isBlank()) {
            return true;
        }

        return VALID_CATEGORIES.contains(trimmedCategory.toUpperCase());
    }
}
