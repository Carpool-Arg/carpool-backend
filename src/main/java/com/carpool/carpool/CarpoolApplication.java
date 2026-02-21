package com.carpool.carpool;

import jakarta.annotation.PostConstruct;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.carpool.carpool.security.config.RecaptchaConfig;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(RecaptchaConfig.class)
public class CarpoolApplication {

	@PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
    }
	public static void main(String[] args) {
		SpringApplication.run(CarpoolApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplateBuilder().build(); //clase para la comunicacion con servicios web RESTful
	}
}
