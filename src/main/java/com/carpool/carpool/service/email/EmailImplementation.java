package com.carpool.carpool.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailImplementation implements IEmailService{

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailImplementation.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String emailCarpool;

    @Override
    public void sendEmail(String to, String title, String subject, String message, String buttonUrl, String buttonText) {
        int maxRetries = 3;
        int retryCount = 0;
        long waitMillis = 2000;

        while(retryCount < maxRetries){
            try {

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

                String body = buildTemplate(to, title, subject, message, buttonUrl);
                helper.setFrom(emailCarpool);
                helper.setTo("anil.agustin06@gmail.com");
                helper.setSubject(subject);
                helper.setText(body, true);

                mailSender.send(mimeMessage);
                return;

            }catch (Exception e){
                retryCount++;
                if (retryCount >= maxRetries) {
                    LOGGER.warn("No se pudo enviar el correo electrónico. Intento numero: {}", retryCount);
                    if(retryCount == 3){
                        LOGGER.error("Al intentar por última vez el envio del correo electrónico. Motivo: ",e.getMessage());
                    }
                }
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private String buildTemplate(String title, String subject, String message, String buttonUrl, String buttonText){
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("subject", subject);
        context.setVariable("message", message);
        context.setVariable("buttonUrl", "google.com");
        context.setVariable("buttonText", buttonText);
        return templateEngine.process("email-template", context);
    }

}

