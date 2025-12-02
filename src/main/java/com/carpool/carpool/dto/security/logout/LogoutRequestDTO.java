package com.carpool.carpool.dto.security.logout;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para solicitud de logout")
public class LogoutRequestDTO {
    @Schema(description = "Refresh token que se desea invalidar")
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
