package com.carpool.carpool.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserTokenRequestDTO {

    @NotBlank(message = "El token no puede quedar en blanco.")
    private String token;
}
