package com.spring.eventhub.common.validator;




import java.time.Instant;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FutureDateValidator  implements ConstraintValidator<FutureDate,Instant>{

     @Override
    public void initialize(FutureDate constraintAnnotation) {
        // Inicialización opcional si pasas parámetros a la anotación
    }
    
    @Override
     public boolean isValid(Instant value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; 
        }
        return value.isAfter(Instant.now());

    }
   
}
