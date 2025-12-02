package com.carpool.carpool.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "recaptcha")
public record RecaptchaConfig (String verifyUrl, String secretKey) {
}
