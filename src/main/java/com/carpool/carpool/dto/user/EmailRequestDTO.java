package com.carpool.carpool.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class EmailRequestDTO {
    @NotBlank(message = "El correo electrónico no puede quedar en blanco.")
    @Size(max = 75, message = "El correo electrónico debe tener como máximo 75 caracteres.")
    @Schema(example = "pepe123@gmail.com")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "El correo electrónico debe ser una direccón de correo válida.")
    private String email;
}
