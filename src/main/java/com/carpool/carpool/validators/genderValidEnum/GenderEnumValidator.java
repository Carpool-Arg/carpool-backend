package com.carpool.carpool.validators.genderValidEnum;

import com.carpool.carpool.enums.user.UserGenderEnum;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GenderEnumValidator implements ConstraintValidator<GenderValidEnum, UserGenderEnum> {
    
    @Override
    public boolean isValid(UserGenderEnum value, ConstraintValidatorContext context) {
        
        if (value == null) {
            return true;
        }
        for (UserGenderEnum gender : UserGenderEnum.values()) {
            if (gender == value) {
                return true;
            }
        }
        return false;
    }
}
