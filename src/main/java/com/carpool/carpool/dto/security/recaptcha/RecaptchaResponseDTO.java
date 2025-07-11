package com.carpool.carpool.dto.security.recaptcha;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RecaptchaResponseDTO {
    Boolean success;
    String challege_ts;
    String hostname;
    Double score;
    String action;
}
