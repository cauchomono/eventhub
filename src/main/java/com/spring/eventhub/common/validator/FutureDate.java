package com.spring.eventhub.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy =FutureDateValidator.class )
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureDate {

    String message() default "The date must be a valid future date";

    Class<?>[] groups() default{};
    Class< ? extends Payload>[] payload() default{};

}
