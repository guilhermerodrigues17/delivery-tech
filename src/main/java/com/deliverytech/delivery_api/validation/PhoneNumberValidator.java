package com.deliverytech.delivery_api.validation;

import com.deliverytech.delivery_api.validation.annotations.ValidPhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    private final Pattern PHONE_PATTERN = Pattern.compile("^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$");

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null) {
            return true;
        }

        var trimmedPhone = phoneNumber.trim();
        if (trimmedPhone.isBlank()) {
            return true;
        }

        return PHONE_PATTERN.matcher(trimmedPhone).matches();
    }
}
