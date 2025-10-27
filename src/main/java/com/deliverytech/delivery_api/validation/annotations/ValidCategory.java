package com.deliverytech.delivery_api.validation.annotations;

import com.deliverytech.delivery_api.validation.CategoryValidator;
import com.deliverytech.delivery_api.validation.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CategoryValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCategory {
    String message() default "Categoria inválida.";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
