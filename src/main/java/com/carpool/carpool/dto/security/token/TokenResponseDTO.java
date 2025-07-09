package com.carpool.carpool.dto.security.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TokenResponseDTO {
    String accessToken;
    String refreshToken;
}
