package com.carpool.carpool.dto.user;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class UserActivationRequestDTO {
    private String email;
}
