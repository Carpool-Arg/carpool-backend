package com.carpool.carpool.dto.security.recaptcha;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RecaptchaResponseDTO {
    Boolean success;
    String challenge_ts;
    String hostname;
    Double score;
    String action;
    List<String> errorCodes;
}
