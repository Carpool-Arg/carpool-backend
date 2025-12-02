package com.carpool.carpool.validators.yearNotInFuture;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;
public class YearNotInFutureValidator implements ConstraintValidator<YearNotInFuture, Integer>  {

  /*
   * Implementa la interfaz ConstraintValidator para validar que el año proporcionado
   * no sea mayor al año actual.
   */
    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext context) {
        /* 
         * Valida que el año proporcionado no sea mayor al año actual.
         * Si el año es nulo, se asume que la validación será manejada
         * por otra anotación como @NotNull.
         * Si el año es mayor al año actual, devuelve false, indicando que la validación ha fallado.
         */
        if (year == null) {
            return true; 
        }
        return year <= Year.now().getValue();
    }
    

}
