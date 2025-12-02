package com.carpool.carpool.dto.user;


import com.carpool.carpool.enums.user.UserGenderEnum;
import com.carpool.carpool.validators.genderValidEnum.GenderValidEnum;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileUpdateRequestDTO {
    
    @GenderValidEnum
    private UserGenderEnum gender; 

    @Size(min = 7, max = 25, message = "El número de teléfono debe tener entre 7 y 50 caracteres.")
    @Pattern(regexp = "^[0-9\\-+\\s]*$", message = "El número de teléfono debe contener únicamente números, guiones, signos + y espacios.")
    private String phone;

    private boolean removeProfileImage; 
}
