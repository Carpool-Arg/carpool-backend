package com.carpool.carpool.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPasswordChangeRequestDTO {

    @NotBlank(message = "La contraseña actual no puede quedar en blanco.")
    private String oldPassword;

    @NotBlank(message = "La nueva contraseña no puede quedar en blanco.")
    @Size(min = 8, max = 255, message = "La nueva contraseña debe tener entre 6 y 255 caracteres.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La nueva contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    private String newPassword;

    @NotBlank(message = "La confirmación de la nueva contraseña no puede quedar en blanco.")
    private String confirmNewPassword;
}


