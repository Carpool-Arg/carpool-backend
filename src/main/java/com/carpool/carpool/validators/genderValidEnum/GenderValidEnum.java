package com.carpool.carpool.validators.genderValidEnum;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenderEnumValidator.class)
public @interface GenderValidEnum {
    String message() default "El género no es válido. Debe ser MALE, FEMALE o UNSPECIFIED.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
