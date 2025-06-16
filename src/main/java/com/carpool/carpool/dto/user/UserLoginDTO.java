package com.carpool.carpool.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDTO {

    @NotBlank(message = "El nombre de usuario no puede quedar en blanco.")
    @NotNull(message = "El nombre de usuario no puede ser nulo.")
    @Size(min = 6, message = "El nombre de usuario debe tener al menos 6 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El nombre de usuario debe contener únicamente letras, números y guiones bajos.")
    private String username;

    @NotNull(message = "La contraseña no puede ser nula.La contraseña no puede ser nula.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    private String password;
}
