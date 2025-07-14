package com.carpool.carpool.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailImplementation implements IEmailService{

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String html) {
        int maxRetries = 3;
        int retryCount = 0;
        long waitMillis = 2000;

        while(retryCount < maxRetries){
            try {
                String body = "Prueba de correo electronico";
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("argcarpool@gmail.com");
                message.setTo(to);
                message.setSubject("Prueba");
                message.setText(body);

                mailSender.send(message);
                return;

            }catch (Exception e){
                retryCount++;

                if (retryCount >= maxRetries) {
                    throw new RuntimeException("No se pudo enviar el correo: " + e.getMessage());
                }
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // buena práctica: volver a marcar el hilo como interrumpido
                    throw new RuntimeException("Reintento interrumpido");
                }
            }
        }
    }
}
