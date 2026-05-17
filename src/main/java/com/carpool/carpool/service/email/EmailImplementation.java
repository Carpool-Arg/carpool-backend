package com.carpool.carpool.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
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
    public void sendEmail(String to, String subject, String title, String message, String optionalMessage, String buttonUrl, String buttonText, String messageFooter) {
        int maxRetries = 3;
        int retryCount = 0;
        long waitMillis = 2000;

        while(retryCount < maxRetries){
            try {

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

                String body = buildTemplate(title, message, optionalMessage, buttonUrl, buttonText, messageFooter);
                helper.setFrom(emailCarpool);
                helper.setTo(to);
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

    @Override
    public void sendEmailWithAttachment(
            String to, String subject, String title, String message,
            String optionalMessage, String buttonUrl, String buttonText,
            String messageFooter, byte[] attachmentBytes, String attachmentFilename) {

        int maxRetries = 3;
        int retryCount = 0;
        long waitMillis = 2000;

        while (retryCount < maxRetries) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                // multipart = true para poder adjuntar archivos
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

                String body = buildTemplate(title, message, optionalMessage, buttonUrl, buttonText, messageFooter);
                helper.setFrom(emailCarpool);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(body, true);

                // Adjuntar el PDF
                helper.addAttachment(
                        attachmentFilename,
                        new ByteArrayResource(attachmentBytes),
                        "application/pdf"
                );

                mailSender.send(mimeMessage);
                return;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    LOGGER.error("Error enviando email con adjunto a {}: {}", to, e.getMessage());
                }
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Metodo que setea valores a la planilla html.
     * @param title Titulo del contenido
     * @param message Mensaje principal del body
     * @param optionalMessage Mensaje opcional (usar si se desea agregar contenido de relleno)
     * @param buttonUrl Direccion a donde se va a redirigir al usuario en caso de seleccionar el botón
     * @param buttonText Nombre que va a contener el botón
     * @param messageFooter Mensaje del pie del contenido
     * @return {@link String}
     */
    private String buildTemplate(String title, String message, String optionalMessage, String buttonUrl, String buttonText, String messageFooter){
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("message", message);
        context.setVariable("optionalMessage", optionalMessage);
        context.setVariable("buttonUrl", buttonUrl);
        context.setVariable("buttonText", buttonText);
        context.setVariable("messageFooter", messageFooter);
        return templateEngine.process("email-template", context);
    }
}

