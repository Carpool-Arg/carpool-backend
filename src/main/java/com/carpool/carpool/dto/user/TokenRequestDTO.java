package com.carpool.carpool.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TokenRequestDTO {

    @NotBlank(message = "El token no puede quedar en blanco.")
    private String token;
}
