package com.carpool.carpool.dto.user;

import jakarta.persistence.Column;
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
public class UserRequestDTO {

    @NotNull(message = "El nombre no puede ser nulo.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El nombre debe contener sólo letras y espacios.")
    private String name;

    @NotNull(message = "El apellido no puede ser nulo.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El apellido debe contener sólo letras y espacios.")
    private String lastname;

    @NotBlank(message = "El nombre de usuario no puede quedar en blanco.")
    @NotNull(message = "El nombre de usuario no puede ser nulo.")
    @Size(min = 6, message = "El nombre de usuario debe tener al menos 6 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El nombre de usuario debe contener únicamente letras, números y guiones bajos.")
    private String username;

    @NotNull(message = "El correo electrónico no puede ser nulo.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "El correo electrónico debe ser una direccón de correo válida.")
    private String email;

    @NotNull(message = "La contraseña no puede ser nula.La contraseña no puede ser nula.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    private String password;

    @NotNull(message = "La contraseña no puede ser nula.La contraseña no puede ser nula.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    private String confirmPassword;

    @NotNull(message = "El número del DNI no puede ser nulo.")
    @Pattern(regexp = "^[0-9]{8}$", message = "El número de DNI debe contener 8 dígitos.")
    private String dni;

    @NotNull(message = "El número de teléfono no puede ser nulo.")
    @Pattern(regexp = "^[0-9\\-\\+\\s]*$", message = "El número de teléfono debe contener únicamente números, guiones, signos + y espacios.")
    private String phone;
}
