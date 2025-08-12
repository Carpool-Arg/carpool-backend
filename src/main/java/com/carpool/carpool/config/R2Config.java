package com.carpool.carpool.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


import java.net.URI;

@Configuration
public class R2Config {

    @Value("${cloudflare.r2.endpoint}")
    private String urlR2;

    @Value("${cloudflare.r2.access-key}")
    private String R2AccessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String R2SecretKey;

    /**
     * Esta clase se encarga de configurar el cliente R2 de Cloudflare. A grandes rasgos, se detalla la URL (con sus credenciales)
     * para que en otras partes del codigo se pueda obtener o realizar otra accion, como insercion, de recursos.
     * @return Cliente S3 del tipo {@link S3Client}
     */
    @Bean
    public S3Client r2S3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(urlR2))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(R2AccessKey, R2SecretKey)
                        )
                )
                .region(Region.of("auto"))
                .build();
    }
}
