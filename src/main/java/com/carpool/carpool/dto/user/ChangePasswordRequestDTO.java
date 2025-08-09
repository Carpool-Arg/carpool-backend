package com.carpool.carpool.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChangePasswordRequestDTO {

    
    @NotBlank(message = "La contraseña no puede quedar en blanco.")
    @Size(min = 6, max = 255, message = "La contraseña debe tener entre 6 y 255 caracteres.")
    @Schema(example = "pJUan22")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    private String password;

    @NotBlank(message = "La contraseña no puede quedar en blanco.")
    @Schema(example = "pJUan22")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    private String confirmPassword;

    @NotBlank(message = "El token no puede estar en blanco.")
    private String token;
}
