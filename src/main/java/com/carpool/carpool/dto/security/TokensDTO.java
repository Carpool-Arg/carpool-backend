package com.carpool.carpool.dto.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TokensDTO {
    String accesToken;
    String refreshToken;
}
