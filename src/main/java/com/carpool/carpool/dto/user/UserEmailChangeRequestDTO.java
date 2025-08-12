package com.carpool.carpool.dto.user;

import jakarta.validation.constraints.NotBlank;
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
public class UserEmailChangeRequestDTO {

    @NotBlank(message = "El nuevo correo electrónico no puede quedar en blanco.")
    @Size(max = 75, message = "El nuevo correo electrónico debe tener como máximo 75 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "El nuevo correo electrónico debe ser una direccón de correo válida.")
    private String newEmail;
}
