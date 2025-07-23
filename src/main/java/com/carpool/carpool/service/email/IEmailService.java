package com.carpool.carpool.service.email;

public interface IEmailService {
    void sendEmail(String to, String subject, String title, String message, String optionalMessage, String buttonUrl, String buttonText, String messageFooter);
}
