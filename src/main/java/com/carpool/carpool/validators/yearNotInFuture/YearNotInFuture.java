package com.carpool.carpool.validators.yearNotInFuture;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = YearNotInFutureValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
/*
 * Esta anotación se utiliza para validar que el año proporcionado no sea mayor al año actual.
 * Se usa en campos de tipo Integer o Long.
 * La validación se realiza mediante la clase YearNotInFutureValidator.
 */
public @interface YearNotInFuture {
    
    String message() default "El año no puede ser mayor al actual."; //Mensaje de error por defecto
    Class<?>[] groups() default {}; // Grupos de validación a los que pertenece esta anotación
    Class<? extends Payload>[] payload() default {}; // Carga útil para la anotación, se puede usar para transportar información adicional
}
