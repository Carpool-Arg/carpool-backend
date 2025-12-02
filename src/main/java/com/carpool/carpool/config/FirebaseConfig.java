package com.carpool.carpool.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${TYPE}")
    private String type;

    @Value("${PROJECT_ID}")
    private String projectId;

    @Value("${PRIVATE_KEY_ID}")
    private String privateKeyId;

    @Value("${PRIVATE_KEY}")
    private String privateKey;

    @Value("${CLIENT_EMAIL}")
    private String clientEmail;

    @Value("${CLIENT_ID}")
    private String clientId;

    @Value("${AUTH_URI}")
    private String authUri;

    @Value("${TOKEN_URI}")
    private String tokenUri;

    @Value("${AUTH_PROVIDER_X509_CERT_URL}")
    private String authProviderCertUrl;

    @Value("${CLIENT_X509_CERT_URL}")
    private String clientCertUrl;

    @Value("${UNIVERSE_DOMAIN}")
    private String universeDomain;

    @PostConstruct
    public void initFirebase() throws IOException {
        String json = String.format("""
        {
          "type": "%s",
          "project_id": "%s",
          "private_key_id": "%s",
          "private_key": "%s",
          "client_email": "%s",
          "client_id": "%s",
          "auth_uri": "%s",
          "token_uri": "%s",
          "auth_provider_x509_cert_url": "%s",
          "client_x509_cert_url": "%s",
          "universe_domain": "%s"
        }
        """,
                type, projectId, privateKeyId, privateKey, clientEmail, clientId,
                authUri, tokenUri, authProviderCertUrl, clientCertUrl, universeDomain
        );

        try (InputStream serviceAccount = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }
    }
}
