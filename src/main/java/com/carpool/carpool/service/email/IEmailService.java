package com.carpool.carpool.service.email;

public interface IEmailService {
    void sendEmail(String to, String title, String subject, String message, String buttonUrl, String buttonText);
}
