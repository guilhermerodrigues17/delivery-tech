package com.deliverytech.delivery_api.validation;

import com.deliverytech.delivery_api.validation.annotations.ValidCEP;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class CepValidator implements ConstraintValidator<ValidCEP, String> {

    private final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");

    @Override
    public void initialize(ValidCEP constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String cep, ConstraintValidatorContext context) {
        if (cep == null) {
            return true;
        }

        var trimmedCep = cep.trim();
        if(trimmedCep.isBlank()) {
            return true;
        }

        return CEP_PATTERN.matcher(trimmedCep).matches();
    }
}
