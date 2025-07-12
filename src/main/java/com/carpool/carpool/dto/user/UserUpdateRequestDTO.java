package com.carpool.carpool.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequestDTO {

    @NotBlank(message = "El apellido no puede quedar en blanco.")
    @Size(min = 1, max = 100, message = "El apellido debe tener entre 1 y 100 caracter.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El apellido debe contener sólo letras y espacios.")
    @Schema(example = "Pérez")
    private String lastname;

    @NotBlank(message = "El nombre de usuario no puede quedar en blanco.")
    @Size(min = 3, max = 25, message = "El nombre de usuario debe tener entre 3 y 25 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El nombre de usuario debe contener únicamente letras, números y guiones bajos.")
    @Schema(example = "JuanP123")
    private String username;

    @NotBlank(message = "La contraseña no puede quedar en blanco.")
    @Size(min = 6, max = 255, message = "La contraseña debe tener entre 6 y 255 caracteres.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    @Schema(example = "pJUan22")
    private String password;

    @NotBlank(message = "La contraseña no puede quedar en blanco.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    @Schema(example = "pJUan22")
    private String confirmPassword;

    @NotBlank(message = "El número del DNI no puede quedar en blanco.")
    @Size(min = 7, max = 50, message = "El número del DNI debe tener entre 7 y 50 caracteres.")
    @Pattern(regexp = "^[0-9]+$", message = "El número del DNI debe contener únicamente números.")
    @Schema(example = "12345678")
    private String dni;

    @NotBlank(message = "El número de teléfono no puede quedar en blanco.")
    @Size(min = 7, max = 25, message = "El número de teléfono debe tener entre 7 y 50 caracteres.")
    @Pattern(regexp = "^[0-9\\-+\\s]*$", message = "El número de teléfono debe contener únicamente números, guiones, signos + y espacios.")
    @Schema(example = "3534222456")
    private String phone;
}
