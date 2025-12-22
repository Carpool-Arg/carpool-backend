package com.carpool.carpool.dto.user;

import java.time.LocalDate;

import com.carpool.carpool.enums.user.UserGenderEnum;
import com.carpool.carpool.validators.genderValidEnum.GenderValidEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    @NotBlank(message = "El nombre no puede quedar en blanco.")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracter.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El nombre debe contener sólo letras y espacios.")
    private String name;

    @NotBlank(message = "El apellido no puede quedar en blanco.")
    @Size(min = 1, max = 100, message = "El apellido debe tener entre 1 y 100 caracter.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El apellido debe contener sólo letras y espacios.")
    private String lastname;

    @NotBlank(message = "El nombre de usuario no puede quedar en blanco.")
    @Size(min = 6, max = 25, message = "El nombre de usuario debe tener entre 6 y 25 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El nombre de usuario debe contener únicamente letras, números y guiones bajos.")
    private String username;

    @NotBlank(message = "El correo electrónico no puede quedar en blanco.")
    @Size(max = 75, message = "El correo electrónico debe tener como máximo 75 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "El correo electrónico debe ser una direccón de correo válida.")
    private String email;

    @NotBlank(message = "La contraseña no puede quedar en blanco.")
    @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres.")
    @Schema(example = "pJUan22")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    private String password;

    @NotBlank(message = "La contraseña no puede quedar en blanco.")
    @Schema(example = "pJUan22")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    private String confirmPassword;

    @NotBlank(message = "El número del DNI no puede quedar en blanco.")
    @Size(min = 7, max = 8, message = "El número del DNI debe tener entre 7 y 50 caracteres.")
    @Pattern(regexp = "^[0-9]+$", message = "El número del DNI debe contener únicamente números.")
    private String dni;

    @NotBlank(message = "El número de teléfono no puede quedar en blanco.")
    @Size(min = 7, max = 25, message = "El número de teléfono debe tener entre 7 y 50 caracteres.")
    @Pattern(regexp = "^[0-9\\-+\\s]*$", message = "El número de teléfono debe contener únicamente números, guiones, signos + y espacios.")
    private String phone;

    @NotNull(message= "La fecha de nacimiento no puede ser nula.") 
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate birthDate;

    @NotNull(message = "El género no puede quedar en blanco.")
    @GenderValidEnum
    private UserGenderEnum gender;
}
