package com.carpool.carpool.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;


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
     * Este metodo se encarga de configurar el cliente R2 de Cloudflare. A grandes rasgos, se detalla la URL (con sus credenciales)
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

    /**
     * Metodo encargado de instanciar un objeto {@link S3Presigner} que permite generar URL de
     * los recursos para que se puedan acceder a los mismos.
     * @return Objeto {@link S3Presigner}
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(urlR2))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(R2AccessKey, R2SecretKey)))
                .region(Region.of("auto"))
                .build();
    }
}
