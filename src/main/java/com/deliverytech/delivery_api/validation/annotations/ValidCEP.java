package com.deliverytech.delivery_api.validation.annotations;

import com.deliverytech.delivery_api.validation.CepValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CepValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCEP {
    String message() default "Formato de CEP inválido. Use XXXXX-XXX ou XXXXXXXX.";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
