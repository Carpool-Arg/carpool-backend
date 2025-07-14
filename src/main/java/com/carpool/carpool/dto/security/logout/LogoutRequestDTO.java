package com.carpool.carpool.dto.security.logout;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequestDTO {
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
